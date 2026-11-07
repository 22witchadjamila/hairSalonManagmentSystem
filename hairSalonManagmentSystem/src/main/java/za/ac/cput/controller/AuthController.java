package za.ac.cput.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.audit.AuditService;
import za.ac.cput.config.LoginAttemptService;
import za.ac.cput.config.PermissionPolicy;
import za.ac.cput.config.RoleSeeder;
import za.ac.cput.config.SessionControl;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.User;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.TooManyAttemptsException;
import za.ac.cput.service.AuthService;

import java.util.OptionalLong;

/**
 * Chapter 9.5.2 Form-Based Authentication.
 *
 * The chapter posts a form to `j_spring_security_check` and the container redirects to
 * login.jsp or the stored URL. A Vue SPA cannot use a redirect - it needs a JSON answer it
 * can act on - so these endpoints do the same three things explicitly:
 *   verify the credentials, establish a session, report who you now are.
 *
 * The session cookie that comes back is the authentication token for every later request,
 * exactly as with the chapter's form login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final PermissionPolicy perm;
    private final LoginAttemptService attempts;
    private final AuditService audit;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final RememberMeServices rememberMe;
    private final SessionControl sessionControl;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          PermissionPolicy perm, LoginAttemptService attempts, AuditService audit,
                          SessionAuthenticationStrategy sessionStrategy, RememberMeServices rememberMe,
                          SessionControl sessionControl) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.perm = perm;
        this.attempts = attempts;
        this.audit = audit;
        this.sessionStrategy = sessionStrategy;
        this.rememberMe = rememberMe;
        this.sessionControl = sessionControl;
    }

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters.") String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank String phoneNumber) {}

    /** rememberMe: the chapter's "Remember me on this computer" tick box. Optional; false if omitted. */
    public record LoginRequest(@NotBlank String username, @NotBlank String password, Boolean rememberMe) {}

    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {}

    /** Who the caller is. Shape is deliberately minimal - no password, no role internals. */
    /**
     * customerId is set only for CUSTOMER logins, stylistId/speciality only for STYLIST logins,
     * neither for ADMIN. The frontend routes each role to its own dashboard from this.
     */
    public record SessionUser(String username, String firstName, String lastName,
                              String email, String role, String customerId,
                              String phoneNumber, java.time.LocalDate dateOfBirth,
                              String stylistId, String speciality,
                              java.util.Set<String> permissions) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionUser register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http,
                                HttpServletResponse response) {
        authService.registerCustomer(request.username(), request.password(), request.firstName(),
                request.lastName(), request.email(), request.phoneNumber());
        // Sign the new customer straight in - having just typed the password, making them
        // type it again is friction with no security benefit.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SessionUser user = startSession(auth, http, response, false);
        audit.recordAs(auth.getName(), user.role(), http, "Registered a customer account", 201, AuditService.SUCCESS, null);
        return user;
    }

    /**
     * Sign in. After five wrong passwords for a username the account is locked for 15 minutes
     * (LoginAttemptService), and every outcome - success, failure, block, lock - is written to
     * the audit log with the username that was typed.
     */
    @PostMapping("/login")
    public SessionUser login(@Valid @RequestBody LoginRequest request, HttpServletRequest http,
                             HttpServletResponse response) {
        String username = request.username().trim();

        OptionalLong lockedFor = attempts.lockedForSeconds(username);
        if (lockedFor.isPresent()) {
            audit.recordAs(username, null, http, "LOGIN_BLOCKED", 429, AuditService.DENIED, "Account is temporarily locked");
            throw new TooManyAttemptsException(lockedFor.getAsLong());
        }

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (AuthenticationException e) {
            boolean nowLocked = attempts.recordFailure(username);
            audit.recordAs(username, null, http, "LOGIN_FAILED", 401, AuditService.DENIED, null);
            if (nowLocked) {
                audit.recordAs(username, null, http, "ACCOUNT_LOCKED", 429, AuditService.DENIED,
                        "Locked for 15 minutes after repeated failed sign-ins");
            }
            // One message for every failure. Distinguishing "no such user" from "wrong
            // password" would let someone enumerate valid usernames.
            throw new BadCredentialsException("Incorrect username or password.");
        }

        attempts.recordSuccess(username);
        boolean remember = Boolean.TRUE.equals(request.rememberMe());
        SessionUser user = startSession(auth, http, response, remember);
        audit.recordAs(auth.getName(), user.role(), http, "LOGIN", 200, AuditService.SUCCESS, remember ? "Remember me" : null);
        return user;
    }

    /**
     * Change your own password. The current password must be right (a wrong guess counts toward the
     * same lockout as a failed sign-in, so a stolen session can't be used to guess it), the new one
     * must pass PasswordPolicy, and every OTHER open session for the account is signed out.
     */
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication,
                               HttpServletRequest http) {
        String username = authentication.getName();
        OptionalLong lockedFor = attempts.lockedForSeconds(username);
        if (lockedFor.isPresent()) throw new TooManyAttemptsException(lockedFor.getAsLong());
        if (!authService.passwordMatches(username, request.currentPassword())) {
            attempts.recordFailure(username);
            throw new InvalidOperationException("Your current password is incorrect.");
        }
        authService.changePassword(username, request.currentPassword(), request.newPassword());
        HttpSession session = http.getSession(false);
        sessionControl.expireSessionsOf(username, session == null ? null : session.getId());
    }

    /**
     * Chapter 9.7.7 "Obtaining information about the current user" - reads the principal out
     * of the SecurityContextHolder. The SPA calls this on page load to find out whether the
     * session cookie it already holds is still valid.
     */
    @GetMapping("/me")
    public ResponseEntity<SessionUser> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(describe(authentication));
    }

    /**
     * Establishes the session after the credentials were verified.
     *
     * Spring applies its SessionAuthenticationStrategy chain to its OWN form login. This endpoint is
     * our own JSON login, so Spring never sees it - and without this call neither session-fixation
     * protection (9.7.5) nor the one-session-per-user rule (9.7.4) would run. The chain does three
     * things: ends the user's other session (max 1), issues a NEW session id so one planted before
     * sign-in is worthless (migrateSession), and registers the session so it can be ended later.
     */
    private SessionUser startSession(Authentication auth, HttpServletRequest http, HttpServletResponse response,
                                     boolean remember) {
        sessionStrategy.onAuthentication(auth, http, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        http.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // "Remember me on this computer": a signed, expiring cookie that signs the person back in
        // when the session has ended. It includes the password hash, so changing the password
        // invalidates every remembered sign-in.
        if (remember) rememberMe.loginSuccess(new RememberMeRequest(http), response, auth);

        return describe(auth);
    }

    /** The remember-me service looks for a form field; our login is JSON, so report it as ticked. */
    private static final class RememberMeRequest extends HttpServletRequestWrapper {
        RememberMeRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return "remember-me".equals(name) ? "true" : super.getParameter(name);
        }
    }

    /**
     * Previously this assumed every login was a customer and threw for anyone else - which
     * would have crashed the first admin or stylist login. Each role now reports what it has.
     */
    private SessionUser describe(Authentication authentication) {
        String username = authentication.getName();
        User user = authService.userFor(username);
        String role = user.getRole() != null ? user.getRole().getName() : null;

        Customer customer = RoleSeeder.CUSTOMER.equals(role)
                ? authService.findCustomerFor(username).orElse(null) : null;
        Stylist stylist = RoleSeeder.STYLIST.equals(role)
                ? authService.findStylistFor(username).orElse(null) : null;

        String phone = customer != null && customer.getPhoneNumber() != null ? customer.getPhoneNumber().getValue()
                : stylist != null && stylist.getPhoneNumber() != null ? stylist.getPhoneNumber().getValue()
                : null;

        return new SessionUser(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getEmail().getValue(), role,
                customer != null ? customer.getCustomerId() : null,
                phone,
                customer != null ? customer.getDateOfBirth() : null,
                stylist != null ? stylist.getStylistId() : null,
                stylist != null ? stylist.getSpeciality() : null,
                // What the dashboard may show. Only a hint for the UI: every request is still
                // checked on the server by PermissionPolicy.
                perm.permissionsOf(authentication));
    }
}
