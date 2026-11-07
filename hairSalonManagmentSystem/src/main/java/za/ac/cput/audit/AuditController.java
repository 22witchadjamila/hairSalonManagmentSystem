package za.ac.cput.audit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The audit trail, newest first. ADMIN only - and deliberately NOT one of the permission modules a
 * custom role can be given, so the people the log watches can't be handed the ability to read it.
 * There is no way to edit or delete an entry through the API.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService audit;

    public AuditController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping
    public List<AuditEvent> latest(@RequestParam(name = "limit", defaultValue = "200") int limit) {
        return audit.latest(limit);
    }
}
