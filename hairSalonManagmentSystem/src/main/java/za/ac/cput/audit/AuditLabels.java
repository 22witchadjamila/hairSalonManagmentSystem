package za.ac.cput.audit;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Turns "POST /api/payments/ABC123/refund" into "Refunded a payment" for the audit log. A request
 * with no entry here is still recorded - as "POST /api/..." - so a new endpoint can never slip
 * through unlogged just because nobody labelled it.
 */
public final class AuditLabels {

    private record Rule(String method, String pattern, String label) {}

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<Rule> RULES = List.of(
            // appointments
            new Rule("POST", "/api/appointments/book", "Booked an appointment"),
            new Rule("POST", "/api/appointments/*/confirm", "Confirmed an appointment"),
            new Rule("POST", "/api/appointments/*/cancel", "Cancelled an appointment"),
            new Rule("POST", "/api/appointments/*/complete", "Completed an appointment"),
            new Rule("DELETE", "/api/appointments/*", "Deleted an appointment"),
            // money
            new Rule("POST", "/api/payments/record", "Recorded a payment"),
            new Rule("POST", "/api/payments/process", "Recorded a payment"),
            new Rule("POST", "/api/payments/*/refund", "Refunded a payment"),
            new Rule("DELETE", "/api/payments/*", "Deleted a payment record"),
            new Rule("POST", "/api/loyalty-rewards/customer/*/add-points", "Added loyalty points"),
            // promotions
            new Rule("POST", "/api/promotions", "Created a promotion"),
            new Rule("PUT", "/api/promotions/*", "Edited a promotion"),
            new Rule("POST", "/api/promotions/*/end", "Ended a promotion"),
            new Rule("POST", "/api/promotions/*/reopen", "Reopened a promotion"),
            new Rule("DELETE", "/api/promotions/*", "Deleted a promotion"),
            // catalogue and people
            new Rule("POST", "/api/services", "Added a service"),
            new Rule("PUT", "/api/services/*", "Edited a service"),
            new Rule("DELETE", "/api/services/*", "Removed a service"),
            new Rule("POST", "/api/services/*/restore", "Restored a service"),
            new Rule("POST", "/api/stylists", "Added a stylist"),
            new Rule("PUT", "/api/stylists/*", "Edited a stylist"),
            new Rule("DELETE", "/api/stylists/*", "Removed a stylist"),
            new Rule("POST", "/api/stylists/*/restore", "Restored a stylist"),
            new Rule("POST", "/api/customers", "Added a customer"),
            new Rule("PUT", "/api/customers/*", "Edited a customer"),
            new Rule("DELETE", "/api/customers/*", "Deleted a customer"),
            new Rule("POST", "/api/products", "Added a product"),
            new Rule("PUT", "/api/products/*", "Edited a product"),
            new Rule("DELETE", "/api/products/*", "Deleted a product"),
            new Rule("POST", "/api/products/*/adjust-stock", "Adjusted stock"),
            new Rule("POST", "/api/products/*/for-sale", "Changed whether a product is for sale"),
            // accounts and access
            new Rule("POST", "/api/users", "Created a staff login"),
            new Rule("PUT", "/api/users/*/role", "Changed a login's role"),
            new Rule("PUT", "/api/users/*/password", "Reset a user's password"),
            new Rule("DELETE", "/api/users/*", "Deleted a login"),
            new Rule("POST", "/api/roles", "Created a role"),
            new Rule("PUT", "/api/roles/*", "Edited a role"),
            new Rule("DELETE", "/api/roles/*", "Deleted a role"),
            new Rule("POST", "/api/admin/stylists/*/account", "Created a stylist login"),
            new Rule("POST", "/api/auth/change-password", "Changed own password"),
            // messages and feedback
            new Rule("POST", "/api/notifications/promotion", "Sent a promotion message"),
            new Rule("POST", "/api/notifications/appointment-reminder", "Scheduled a reminder"),
            new Rule("DELETE", "/api/notifications/*", "Deleted a notification"),
            new Rule("POST", "/api/feedback", "Left feedback"),
            new Rule("DELETE", "/api/feedback/*", "Deleted feedback"));

    private AuditLabels() {}

    public static String labelFor(String method, String path) {
        for (Rule r : RULES) {
            if (r.method().equalsIgnoreCase(method) && MATCHER.match(r.pattern(), path)) return r.label();
        }
        return method + " " + path;
    }
}
