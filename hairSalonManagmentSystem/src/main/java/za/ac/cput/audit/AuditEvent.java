package za.ac.cput.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * One row of the audit trail: WHO did WHAT, WHEN, from WHERE, and whether it worked.
 *
 * The chapter's "non-repudiation" (a user can't deny having done something) and "auditing" (a
 * tamper-resistant record of security-related events). Rows are only ever inserted - there is no
 * update or delete anywhere in the application - and are shown to admins only.
 *
 * action, outcome and role are plain strings, not enums: the type column of an enum in MySQL is
 * fixed when the table is first created, so adding a new action later would be refused.
 */
@Entity
@Table(name = "audit_log", indexes = @Index(name = "idx_audit_time", columnList = "occurredAt"))
public class AuditEvent {

    @Id
    private String auditId;
    private LocalDateTime occurredAt;
    /** The signed-in username, the username typed on a failed sign-in, or "anonymous". */
    private String actor;
    private String role;
    /** A readable label, e.g. "Refunded a payment" - or LOGIN / LOGIN_FAILED / ACCOUNT_LOCKED. */
    private String action;
    private String method;
    @Column(length = 255)
    private String path;
    private Integer status;
    /** SUCCESS, FAILED (the server refused or errored) or DENIED (not allowed). */
    private String outcome;
    private String ip;
    @Column(length = 255)
    private String detail;

    protected AuditEvent() {}

    public AuditEvent(String auditId, LocalDateTime occurredAt, String actor, String role, String action,
                      String method, String path, Integer status, String outcome, String ip, String detail) {
        this.auditId = auditId;
        this.occurredAt = occurredAt;
        this.actor = actor;
        this.role = role;
        this.action = action;
        this.method = method;
        this.path = cut(path, 255);
        this.status = status;
        this.outcome = outcome;
        this.ip = ip;
        this.detail = cut(detail, 255);
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    public String getAuditId()           { return auditId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getActor()             { return actor; }
    public String getRole()              { return role; }
    public String getAction()            { return action; }
    public String getMethod()            { return method; }
    public String getPath()              { return path; }
    public Integer getStatus()           { return status; }
    public String getOutcome()           { return outcome; }
    public String getIp()                { return ip; }
    public String getDetail()            { return detail; }
}
