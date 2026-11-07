package za.ac.cput.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

/**
 * Records every request that CHANGES something (POST, PUT, PATCH, DELETE) under /api - who, what,
 * and whether it worked - without any controller having to remember to ask.
 *
 * Reads are not recorded (they would drown the log). Sign-in, sign-up and sign-out are skipped
 * here because AuthController and SecurityConfig record them explicitly with the username typed.
 * Request bodies are never logged: they can contain passwords.
 *
 * Requests blocked by the URL rules never reach a controller, so SecurityConfig's access-denied
 * handler records those separately.
 */
@Configuration
public class AuditInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> RECORDED_ELSEWHERE =
            Set.of("/api/auth/login", "/api/auth/register", "/api/auth/logout");

    private final AuditService audit;

    public AuditInterceptor(AuditService audit) {
        this.audit = audit;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/**");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!MUTATING.contains(request.getMethod())) return;
        String path = request.getRequestURI();
        if (RECORDED_ELSEWHERE.contains(path)) return;
        int status = response.getStatus();
        String outcome = status < 400 ? AuditService.SUCCESS
                : (status == 401 || status == 403) ? AuditService.DENIED : AuditService.FAILED;
        audit.record(request, AuditLabels.labelFor(request.getMethod(), path), status, outcome, null);
    }
}
