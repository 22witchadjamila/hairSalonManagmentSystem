package za.ac.cput.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import za.ac.cput.util.Helper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Writes the audit trail. Recording must NEVER get in the way of the thing being recorded: any
 * failure here is logged and swallowed, so a full or unavailable audit table can't stop someone
 * from signing in or a payment from being recorded.
 */
@Service
public class AuditService {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String DENIED = "DENIED";

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final int MAX_PAGE = 500;

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    /** An event for the request being handled, attributed to whoever is signed in right now. */
    public void record(HttpServletRequest request, String action, int status, String outcome, String detail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        recordAs(actorOf(auth), roleOf(auth), request, action, status, outcome, detail);
    }

    /** An event with an explicit actor - used for sign-in attempts, where nobody is signed in yet. */
    public void recordAs(String actor, String role, HttpServletRequest request, String action, int status,
                         String outcome, String detail) {
        try {
            repository.save(new AuditEvent(Helper.generateId(), LocalDateTime.now(), actor, role, action,
                    request.getMethod(), request.getRequestURI(), status, outcome,
                    // The directly connected address only. X-Forwarded-For is client-controlled
                    // text, so trusting it would let anyone write any address into the log.
                    request.getRemoteAddr(), detail));
        } catch (Exception e) {
            log.warn("Could not write an audit event ({}): {}", action, e.getMessage());
        }
    }

    public List<AuditEvent> latest(int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE);
        return repository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, size));
    }

    public static String actorOf(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return "anonymous";
        return auth.getName();
    }

    public static String roleOf(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_") && !a.equals("ROLE_ANONYMOUS"))
                .map(a -> a.substring(5)).findFirst().orElse(null);
    }
}
