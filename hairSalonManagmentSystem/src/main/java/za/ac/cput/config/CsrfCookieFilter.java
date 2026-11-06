package za.ac.cput.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring only creates the CSRF token (and so sets the XSRF-TOKEN cookie) when something asks for
 * it. A single-page app never renders a form that would, so this asks on every request: the first
 * page load or /api/auth/me call hands the browser its token, which the app then echoes back in
 * the X-XSRF-TOKEN header on every POST, PUT and DELETE.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) token.getToken();   // loading it is what sets the cookie
        chain.doFilter(request, response);
    }
}
