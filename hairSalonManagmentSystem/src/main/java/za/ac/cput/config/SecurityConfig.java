package za.ac.cput.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import za.ac.cput.repository.UserRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Chapter 9 - ApplicationContextSecurity.xml, expressed as Java configuration.
 *
 * The chapter's file has four sections; this class covers B, C and D:
 *   Section B  <global-method-security>  -> @EnableMethodSecurity below
 *   Section C  <http> + <intercept-url>  -> securityFilterChain()
 *   Section D  <authentication-manager>  -> AppUserDetailsService + the PasswordEncoder here
 * (Section A was XML namespace plumbing and has no Java equivalent.)
 */
@Configuration
// Section B: enables @PreAuthorize / @Secured on service and controller methods (9.7.6).
@EnableMethodSecurity
public class SecurityConfig {

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PermissionPolicy perm,
                                                   UserRepository users) throws Exception {
        http
            // CSRF is disabled because the API is called by a same-origin SPA with no cookie-
            // based form posts. See the write-up: this is the one trade-off worth revisiting.
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                // Chapter 9.7.5 - session fixation protection. migrateSession is the chapter's
                // default: a brand new session id is issued at login, so a session id an
                // attacker planted beforehand is useless.
                session.sessionFixation(fixation -> fixation.migrateSession());
                // Chapter 9.7.4 - concurrent session control, max-sessions="1".
                session.maximumSessions(1);
            })

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
                // PermissionPolicy). Paths outside every module - users, roles, /api/admin and
                // anything new - stay ADMIN-only.
                .requestMatchers("/api/**").access(perm.orRoles(RoleSeeder.ADMIN))

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
            // JSON, so a redirect to HTML would be useless to it - return the status code and
            // let the Vue router decide where to send the person.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))   // 401 - not logged in
                .accessDeniedHandler((req, res, e) -> res.sendError(HttpStatus.FORBIDDEN.value(),
                        "You do not have permission to perform this action."))                 // 403 - wrong role
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            // Re-check every signed-in request against the database: a deleted, disabled or
            // re-roled login loses its old session immediately (see SessionFreshnessFilter).
            .addFilterBefore(new SessionFreshnessFilter(users), AuthorizationFilter.class)

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
