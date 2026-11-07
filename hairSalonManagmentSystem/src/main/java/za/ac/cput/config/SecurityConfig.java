package za.ac.cput.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import za.ac.cput.audit.AuditService;
import za.ac.cput.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Chapter 9 - ApplicationContextSecurity.xml, expressed as Java configuration.
 *
 * The chapter's file has four sections; this class covers B, C and D:
 *   Section B  <global-method-security>  -> @EnableMethodSecurity below
 *   Section C  <http> + <intercept-url>  -> securityFilterChain()
 *   Section D  <authentication-manager>  -> AppUserDetailsService + the PasswordEncoder here
 * (Section A was XML namespace plumbing and has no Java equivalent.)
 *
 * docs/SECURITY.md maps every section of the chapter to the code that implements it.
 */
@Configuration
// Section B: enables @PreAuthorize / @Secured on service and controller methods (9.7.6).
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Signs the "remember me" cookie. Set SALON_REMEMBER_ME_KEY to keep remembered sign-ins across
     * restarts; without it a random key is made at startup, so a restart signs everyone out - the
     * safe default, and nothing secret has to be written into source control.
     */
    private final String rememberMeKey;

    public SecurityConfig(@Value("${salon.remember-me.key:}") String configuredKey) {
        this.rememberMeKey = configuredKey == null || configuredKey.isBlank() ? UUID.randomUUID().toString() : configuredKey;
    }

    /**
     * Chapter 9.7.2 adds a <password-encoder hash="md5"/>.
     *
     * Deliberate deviation: MD5 (and SHA) are plain digests designed to be FAST, which is
     * exactly wrong for passwords - commodity hardware tries billions per second, and the
     * chapter's example is unsalted, so identical passwords produce identical hashes.
     * BCrypt is purpose-built: salted per password and tunably slow. The concept the chapter
     * teaches - never store plain text, encode on the way in - is unchanged.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Exposed so the login endpoint can verify credentials the same way the filter chain does. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ------------------------------------------------------------------ 9.7.4 / 9.7.5 sessions

    /** Which sessions are open for which user - what "max 1 session" is counted against. */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * Chapter 9.7.4: "add the following listener ... to keep Spring Security updated about session
     * lifecycle events" (HttpSessionEventPublisher in web.xml). Without it the registry is never told
     * when a session times out or is invalidated, and keeps counting sessions that no longer exist.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * What Spring runs after a successful form login - and what OUR JSON login must run itself,
     * because Spring never sees it (see AuthController.startSession):
     *   1. concurrent session control, max-sessions="1"  (a second sign-in ends the first)
     *   2. session fixation protection, migrateSession   (a fresh session id at sign-in)
     *   3. register the new session in the registry
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry registry) {
        ConcurrentSessionControlAuthenticationStrategy concurrency = new ConcurrentSessionControlAuthenticationStrategy(registry);
        concurrency.setMaximumSessions(1);
        return new CompositeSessionAuthenticationStrategy(List.of(
                concurrency,
                new SessionFixationProtectionStrategy(),
                new RegisterSessionAuthenticationStrategy(registry)));
    }

    // ------------------------------------------------------------------ remember me

    /**
     * The chapter's login form has a "Remember me on this computer" tick box
     * (_spring_security_remember_me). The cookie holds the username, an expiry and a SHA-256
     * signature over those plus the password hash, so it expires after 14 days and stops working
     * the moment the password changes.
     */
    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices services =
                new TokenBasedRememberMeServices(rememberMeKey, userDetailsService, RememberMeTokenAlgorithm.SHA256);
        services.setMatchingAlgorithm(RememberMeTokenAlgorithm.SHA256);
        services.setTokenValiditySeconds(14 * 24 * 60 * 60);
        services.setCookieName("salon-remember-me");
        return services;
    }

    // ------------------------------------------------------------------ Section C

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PermissionPolicy perm, UserRepository users,
                                                   AuditService audit, Environment env, SessionRegistry registry,
                                                   RememberMeServices rememberMeServices,
                                                   @Value("${salon.http-redirect-port:8080}") int httpPort,
                                                   @Value("${server.port:8080}") int serverPort) throws Exception {
        // CSRF: the token lives in a cookie the page's script can read (XSRF-TOKEN) and is echoed back
        // in the X-XSRF-TOKEN header on every POST/PUT/DELETE. A page on another site can make the
        // browser SEND the cookie, but it can't READ it, so it can't produce the matching header.
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie.sameSite("Lax"));

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)

            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                // Chapter 9.7.5 - session fixation protection (migrateSession).
                session.sessionFixation(fixation -> fixation.migrateSession());
                // Chapter 9.7.4 - concurrent session control, max-sessions="1". A second sign-in
                // ends the first. What the first browser sees next depends on what it asks for:
                //   - an API call gets a 401 with a message, which the app turns into "you were
                //     signed out" at the sign-in page;
                //   - a page or file (index.html, /assets/*.js) is simply asked again. By then the
                //     dead session has been dropped, so it is served to a signed-out visitor.
                // Answering EVERYTHING with a 401 would stop the browser loading the app itself.
                session.sessionConcurrency(concurrency -> concurrency
                        .maximumSessions(1)
                        .sessionRegistry(registry)
                        .expiredSessionStrategy(event -> {
                            String uri = event.getRequest().getRequestURI();
                            if (uri.startsWith("/api/") || !uri.startsWith("/") || uri.startsWith("//")) {
                                writeJson(event.getResponse(), HttpStatus.UNAUTHORIZED,
                                        "You were signed out because this account signed in somewhere else.");
                            } else {
                                String query = event.getRequest().getQueryString();
                                event.getResponse().sendRedirect(uri + (query == null ? "" : "?" + query));
                            }
                        }));
            })

            .rememberMe(remember -> remember.rememberMeServices(rememberMeServices).key(rememberMeKey))

            // Section C: <intercept-url>. ORDER MATTERS - the first matching rule wins.
            //
            // Deny by default: every /api path not explicitly opened below is ADMIN-only, so a
            // newly added endpoint starts locked rather than open. Rules here are coarse (which
            // ROLE may call a path); controller @PreAuthorize adds ownership (WHICH customer or
            // stylist) via AccessRules. Every /api rule must precede the SPA catch-all at the
            // bottom, or a pattern like /*/{path} would swallow /api/roles.
            .authorizeHttpRequests(auth -> auth

                // ---------- public (the chapter's IS_AUTHENTICATED_ANONYMOUSLY) ----------
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Marketing site: price list, the team, reviews, offers and retail products.
                // Each returns a public projection - no emails, phone numbers or cost prices.
                .requestMatchers(HttpMethod.GET,
                        "/api/services", "/api/services/active", "/api/services/{id}",
                        "/api/services/category/*",
                        "/api/stylists/active",
                        "/api/feedback/public",
                        "/api/promotions/available", "/api/promotions/quote",
                        "/api/products/shop").permitAll()

                // ---------- any signed-in user ----------
                .requestMatchers("/api/auth/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/loyalty-rewards/rules").authenticated()

                // ---------- customers (ownership enforced in the controllers) ----------
                .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasRole(RoleSeeder.CUSTOMER)
                .requestMatchers(HttpMethod.GET, "/api/appointments/customer/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/cancel").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.STYLIST, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.GET, "/api/customers/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.PUT, "/api/customers/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.GET, "/api/feedback/customer/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.POST, "/api/feedback").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.GET, "/api/loyalty-rewards/customer/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.GET, "/api/notifications/customer/*").access(perm.orRoles(RoleSeeder.CUSTOMER, RoleSeeder.ADMIN))

                // ---------- stylists (ownership enforced in the controllers) ----------
                .requestMatchers(HttpMethod.GET, "/api/appointments/stylist/*").access(perm.orRoles(RoleSeeder.STYLIST, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/confirm", "/api/appointments/*/complete")
                        .access(perm.orRoles(RoleSeeder.STYLIST, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.GET, "/api/stylists/*").access(perm.orRoles(RoleSeeder.STYLIST, RoleSeeder.ADMIN))
                .requestMatchers(HttpMethod.PUT, "/api/stylists/*").access(perm.orRoles(RoleSeeder.STYLIST, RoleSeeder.ADMIN))
                // Custom roles may search too; results are limited to the modules they can view.
                .requestMatchers(HttpMethod.GET, "/api/search").access((authentication, ctx) ->
                        new AuthorizationDecision(perm.orRoles(RoleSeeder.STYLIST, RoleSeeder.ADMIN)
                                .check(authentication, ctx).isGranted()
                                || !perm.viewableModules(authentication.get()).isEmpty()))

                // ---------- either party to an appointment ----------
                .requestMatchers(HttpMethod.GET, "/api/appointments/*").authenticated()

                // ---------- everything else under /api: staff administration ----------
                // ADMIN, or a custom role whose permissions cover this module + action (see
                // PermissionPolicy). Paths outside every module - users, roles, the audit log,
                // /api/admin and anything new - stay ADMIN-only.
                .requestMatchers("/api/**").access(perm.orRoles(RoleSeeder.ADMIN))

                // ---------- API documentation: admins only ----------
                // These used to match the SPA catch-all below, so /v3/api-docs listed every endpoint
                // of the system to anyone on the network.
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                        .hasRole(RoleSeeder.ADMIN)

                // ---------- the built Vue app (everything below is NOT under /api) ----------
                .requestMatchers(HttpMethod.GET,
                        "/", "/index.html", "/favicon.ico", "/assets/**", "/images/**").permitAll()
                // Dot-less paths forwarded to index.html by SpaFallbackController. The Vue
                // router decides what to show; the API rules above are what actually guard data.
                .requestMatchers(HttpMethod.GET,
                        "/{path:[^.]*}", "/*/{path:[^.]*}", "/*/*/{path:[^.]*}").permitAll()

                .anyRequest().authenticated()
            )

            // The chapter redirects browsers to login.jsp / accessDenied.jsp. A SPA fetches
            // JSON, so a redirect to HTML would be useless to it - return the status code (and a
            // message) and let the Vue router decide where to send the person.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))   // 401 - not logged in
                .accessDeniedHandler((req, res, e) -> {                                        // 403 - wrong role / bad CSRF token
                    boolean csrf = e instanceof CsrfException;
                    // Only signed-in people reach here (anonymous visitors get the 401 above), so
                    // this records who tried something they weren't allowed to.
                    audit.record(req, csrf ? "CSRF_REJECTED" : "ACCESS_DENIED", HttpStatus.FORBIDDEN.value(),
                            AuditService.DENIED, req.getMethod() + " " + req.getRequestURI());
                    writeJson(res, HttpStatus.FORBIDDEN, csrf
                            ? "Your page is out of date. Refresh it and try again."
                            : "You do not have permission to perform this action.");
                })
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, authentication) -> {
                    audit.recordAs(AuditService.actorOf(authentication), AuditService.roleOf(authentication), req,
                            "LOGOUT", HttpStatus.NO_CONTENT.value(), AuditService.SUCCESS, null);
                    res.setStatus(HttpStatus.NO_CONTENT.value());
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            // Re-check every signed-in request against the database: a deleted, disabled or
            // re-roled login loses its old session immediately (see SessionFreshnessFilter).
            .addFilterBefore(new SessionFreshnessFilter(users), AuthorizationFilter.class)

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        // Chapter 9.7.3: requires-channel="https". Only with the "https" profile, so the normal
        // http://localhost:8080 way of running keeps working. Plain-HTTP requests are sent to the
        // HTTPS port (HttpsRedirectConfig opens the plain port just to do that).
        if (env.matchesProfiles("https")) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
            http.portMapper(ports -> ports.http(httpPort).mapsTo(serverPort));
        }

        return http.build();
    }

    private static void writeJson(HttpServletResponse res, HttpStatus status, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status.value() + ",\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
