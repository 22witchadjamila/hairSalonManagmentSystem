package za.ac.cput.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import za.ac.cput.domain.User;
import za.ac.cput.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

/**
 * Ends a session whose login has changed underneath it.
 *
 * A role is copied into the session at sign-in. Without this check, an admin who is demoted to a
 * custom role - or a login that is deleted or disabled - would keep their old access until they
 * happened to sign out. On every signed-in request the session's role is compared with the
 * database; if the login is gone, disabled, or now has a different role, the session is
 * invalidated and the request continues as anonymous (so protected calls answer 401 and the app
 * sends the person to sign in again, picking up the new role).
 *
 * Deliberately not a @Component: Spring Boot would also register it as a plain servlet filter.
 * SecurityConfig creates it and places it inside the security chain.
 */
public class SessionFreshnessFilter extends OncePerRequestFilter {

    private final UserRepository users;

    public SessionFreshnessFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && request.getSession(false) != null && isStale(auth)) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
        }
        chain.doFilter(request, response);
    }

    private boolean isStale(Authentication auth) {
        if ("anonymousUser".equals(auth.getName())) return false;
        Optional<User> user = users.findByUsername(auth.getName());
        if (user.isEmpty() || !user.get().isActive()) return true;
        String role = user.get().getRole() == null ? RoleSeeder.CUSTOMER : user.get().getRole().getName();
        return auth.getAuthorities().stream().noneMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
    }
}
