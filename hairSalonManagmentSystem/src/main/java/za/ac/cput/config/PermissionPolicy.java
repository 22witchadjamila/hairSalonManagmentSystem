package za.ac.cput.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.enums.PermissionAction;
import za.ac.cput.domain.enums.PermissionModule;
import za.ac.cput.repository.UserRepository;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Decides whether a signed-in user may make a staff API request, from their role's permissions.
 *
 *   ADMIN              - everything (unchanged).
 *   CUSTOMER / STYLIST - nothing here; they keep their own fixed, ownership-checked rules.
 *   custom roles       - exactly the MODULE:ACTION permissions an admin ticked for the role.
 *
 * A request is mapped to a module by its path (PermissionModule.apiPrefix) and to an action by
 * its method. Anything that maps to no module - users, roles, /api/admin, or an endpoint added
 * later - is refused for everyone but ADMIN, so the API stays deny-by-default.
 *
 * Permissions are read from the database on every check, not frozen into the session at login,
 * so taking a permission away takes effect on the user's very next request.
 *
 * Used in two places with the same logic: SecurityConfig (URL rules) and controller
 * @PreAuthorize("@perm.granted() or ...") (method rules).
 */
@Component("perm")
public class PermissionPolicy {

    public record Required(PermissionModule module, PermissionAction action) {
        public String key() { return PermissionPolicy.key(module, action); }
    }

    private final UserRepository users;

    public PermissionPolicy(UserRepository users) {
        this.users = users;
    }

    public static String key(PermissionModule module, PermissionAction action) {
        return module.name() + ":" + action.name();
    }

    /** Every permission that exists, in display order. */
    public static Set<String> all() {
        Set<String> out = new LinkedHashSet<>();
        for (PermissionModule m : PermissionModule.values())
            for (PermissionAction a : PermissionAction.values()) out.add(key(m, a));
        return out;
    }

    /**
     * Cleans an admin's selection: unknown strings are refused rather than stored, and any
     * action implies VIEW on the same module (you can't edit what you can't see).
     */
    public static Set<String> normalise(Set<String> requested) {
        Set<String> valid = all();
        Set<String> out = new LinkedHashSet<>();
        if (requested == null) return out;
        for (String raw : requested) {
            String p = raw == null ? "" : raw.trim().toUpperCase();
            if (!valid.contains(p)) throw new IllegalArgumentException("Unknown permission: " + raw);
            out.add(p);
            out.add(p.substring(0, p.indexOf(':')) + ":" + PermissionAction.VIEW.name());
        }
        // Keep the catalogue's order so the stored set reads predictably.
        return valid.stream().filter(out::contains).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Which module and action a request needs, or empty when it is not a module endpoint. */
    public static Optional<Required> requiredFor(String method, String path) {
        for (PermissionModule m : PermissionModule.values()) {
            String prefix = m.getApiPrefix();
            if (!(path.equals(prefix) || path.startsWith(prefix + "/"))) continue;
            String[] rest = Arrays.stream(path.substring(prefix.length()).split("/"))
                    .filter(s -> !s.isEmpty()).toArray(String[]::new);
            PermissionAction action = switch (method.toUpperCase()) {
                case "GET", "HEAD" -> PermissionAction.VIEW;
                case "PUT", "PATCH" -> PermissionAction.EDIT;
                case "DELETE" -> PermissionAction.DELETE;
                // POST /x or POST /x/record creates; POST /x/{id}/cancel acts on an existing row.
                case "POST" -> rest.length <= 1 ? PermissionAction.CREATE : PermissionAction.EDIT;
                default -> null;
            };
            return action == null ? Optional.empty() : Optional.of(new Required(m, action));
        }
        return Optional.empty();
    }

    /** The permissions held by a signed-in user right now. ADMIN holds them all. */
    public Set<String> permissionsOf(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Set.of();
        if (isAdmin(auth)) return all();
        return users.findByUsername(auth.getName())
                .map(u -> u.getRole())
                .filter(r -> r != null && !isBuiltIn(r.getName()))
                .map(Role::getPermissions)
                .orElse(Set.of());
    }

    public boolean allows(Authentication auth, String method, String path) {
        if (auth == null || !auth.isAuthenticated()) return false;
        if (isAdmin(auth)) return true;
        return requiredFor(method, path).map(r -> permissionsOf(auth).contains(r.key())).orElse(false);
    }

    /** For @PreAuthorize: does the current user's role allow the request being handled? */
    public boolean granted() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAdmin(auth)) return true;
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) return false;
        HttpServletRequest req = attrs.getRequest();
        return allows(auth, req.getMethod(), pathOf(req));
    }

    /** True when the user holds at least one VIEW permission - used to open the header search. */
    public boolean canViewAnything() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return permissionsOf(auth).stream().anyMatch(p -> p.endsWith(":" + PermissionAction.VIEW.name()));
    }

    /** Modules the user may VIEW - search results are limited to these. */
    public Set<PermissionModule> viewableModules(Authentication auth) {
        Set<String> held = permissionsOf(auth);
        Set<PermissionModule> out = EnumSet.noneOf(PermissionModule.class);
        for (PermissionModule m : PermissionModule.values())
            if (held.contains(key(m, PermissionAction.VIEW))) out.add(m);
        return out;
    }

    /**
     * URL rule for SecurityConfig: allowed if the user has one of the given built-in roles,
     * otherwise if their role's permissions cover the request.
     */
    public AuthorizationManager<RequestAuthorizationContext> orRoles(String... roles) {
        Set<String> wanted = Arrays.stream(roles).map(r -> "ROLE_" + r).collect(Collectors.toSet());
        return (Supplier<Authentication> supplier, RequestAuthorizationContext ctx) -> {
            Authentication auth = supplier.get();
            boolean byRole = auth != null && auth.isAuthenticated()
                    && auth.getAuthorities().stream().anyMatch(a -> wanted.contains(a.getAuthority()));
            HttpServletRequest req = ctx.getRequest();
            return new AuthorizationDecision(byRole || allows(auth, req.getMethod(), pathOf(req)));
        };
    }

    public static boolean isBuiltIn(String roleName) {
        return RoleSeeder.ADMIN.equals(roleName) || RoleSeeder.CUSTOMER.equals(roleName)
                || RoleSeeder.STYLIST.equals(roleName);
    }

    private static boolean isAdmin(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream().anyMatch(a -> ("ROLE_" + RoleSeeder.ADMIN).equals(a.getAuthority()));
    }

    private static String pathOf(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        return ctx != null && !ctx.isEmpty() && uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
    }
}
