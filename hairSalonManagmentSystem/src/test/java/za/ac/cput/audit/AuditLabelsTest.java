package za.ac.cput.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AuditLabels - readable names for requests")
class AuditLabelsTest {

    @Test
    @DisplayName("names the important actions")
    void namesActions() {
        assertEquals("Refunded a payment", AuditLabels.labelFor("POST", "/api/payments/ABC123/refund"));
        assertEquals("Recorded a payment", AuditLabels.labelFor("POST", "/api/payments/record"));
        assertEquals("Edited a promotion", AuditLabels.labelFor("PUT", "/api/promotions/P1"));
        assertEquals("Deleted a login", AuditLabels.labelFor("DELETE", "/api/users/U1"));
        assertEquals("Reset a user's password", AuditLabels.labelFor("PUT", "/api/users/U1/password"));
        assertEquals("Changed a login's role", AuditLabels.labelFor("PUT", "/api/users/U1/role"));
        assertEquals("Cancelled an appointment", AuditLabels.labelFor("POST", "/api/appointments/A1/cancel"));
        assertEquals("Created a stylist login", AuditLabels.labelFor("POST", "/api/admin/stylists/S1/account"));
    }

    @Test
    @DisplayName("the method must match: a GET is not 'Deleted a login'")
    void methodMatters() {
        assertEquals("GET /api/users/U1", AuditLabels.labelFor("GET", "/api/users/U1"));
    }

    @Test
    @DisplayName("an endpoint nobody labelled is still described, not dropped")
    void unknownFallsBack() {
        assertEquals("POST /api/brand-new/thing", AuditLabels.labelFor("POST", "/api/brand-new/thing"));
    }
}
