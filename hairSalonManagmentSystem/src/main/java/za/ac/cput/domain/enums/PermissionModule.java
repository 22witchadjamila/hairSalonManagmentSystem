package za.ac.cput.domain.enums;

/**
 * Areas of the salon system a custom role can be given access to, each tied to the API path it
 * guards. Users & Roles (/api/users, /api/roles, /api/admin) is deliberately NOT a module: a
 * role that could manage roles could grant itself anything, so that stays ADMIN-only.
 */
public enum PermissionModule {
    APPOINTMENTS("/api/appointments", "Appointments"),
    CUSTOMERS("/api/customers", "Customers"),
    STYLISTS("/api/stylists", "Stylists"),
    SERVICES("/api/services", "Services"),
    PAYMENTS("/api/payments", "Payments"),
    PROMOTIONS("/api/promotions", "Promotions"),
    INVENTORY("/api/products", "Inventory"),
    FEEDBACK("/api/feedback", "Feedback"),
    LOYALTY("/api/loyalty-rewards", "Loyalty rewards"),
    NOTIFICATIONS("/api/notifications", "Notifications");

    private final String apiPrefix;
    private final String label;

    PermissionModule(String apiPrefix, String label) {
        this.apiPrefix = apiPrefix;
        this.label = label;
    }

    public String getApiPrefix() { return apiPrefix; }
    public String getLabel()     { return label; }
}
