package za.ac.cput.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.domain.enums.PermissionAction;
import za.ac.cput.domain.enums.PermissionModule;
import za.ac.cput.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionPolicy - custom role permissions")
class PermissionPolicyTest {

    @Mock UserRepository users;
    @InjectMocks PermissionPolicy policy;

    private Authentication signedIn(String username, String roleName, Set<String> permissions) {
        Role role = new Role.Builder().setRoleId("r-" + roleName).setName(roleName).setPermissions(permissions).build();
        User user = new User.Builder().setUserId("u-" + username).setUsername(username).setRole(role).setActive(true).build();
        lenient().when(users.findByUsername(username)).thenReturn(Optional.of(user));
        return new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("maps method + path to module + action")
    void mapsRequests() {
        assertEquals(new PermissionPolicy.Required(PermissionModule.APPOINTMENTS, PermissionAction.VIEW),
                PermissionPolicy.requiredFor("GET", "/api/appointments").orElseThrow());
        assertEquals(PermissionAction.CREATE, PermissionPolicy.requiredFor("POST", "/api/payments/record").orElseThrow().action());
        assertEquals(PermissionAction.EDIT, PermissionPolicy.requiredFor("POST", "/api/payments/p1/refund").orElseThrow().action());
        assertEquals(PermissionAction.EDIT, PermissionPolicy.requiredFor("POST", "/api/appointments/a1/cancel").orElseThrow().action());
        assertEquals(PermissionAction.EDIT, PermissionPolicy.requiredFor("PUT", "/api/promotions/p1").orElseThrow().action());
        assertEquals(PermissionAction.DELETE, PermissionPolicy.requiredFor("DELETE", "/api/customers/c1").orElseThrow().action());
        assertEquals(PermissionModule.INVENTORY, PermissionPolicy.requiredFor("GET", "/api/products/low-stock").orElseThrow().module());
    }

    @Test
    @DisplayName("users, roles and /api/admin belong to no module - they can never be granted")
    void userAdministrationIsNotAModule() {
        assertTrue(PermissionPolicy.requiredFor("GET", "/api/users").isEmpty());
        assertTrue(PermissionPolicy.requiredFor("POST", "/api/roles").isEmpty());
        assertTrue(PermissionPolicy.requiredFor("PUT", "/api/users/u1/role").isEmpty());
        assertTrue(PermissionPolicy.requiredFor("POST", "/api/admin/stylists/s1/account").isEmpty());
        // A prefix look-alike must not match a module.
        assertTrue(PermissionPolicy.requiredFor("GET", "/api/appointmentsX").isEmpty());
    }

    @Test
    @DisplayName("a custom role gets exactly what was ticked - no more")
    void customRoleGetsOnlyItsPermissions() {
        Authentication reception = signedIn("rita", "RECEPTION", Set.of("APPOINTMENTS:VIEW", "APPOINTMENTS:EDIT"));

        assertTrue(policy.allows(reception, "GET", "/api/appointments"));
        assertTrue(policy.allows(reception, "POST", "/api/appointments/a1/confirm"));
        assertFalse(policy.allows(reception, "DELETE", "/api/appointments/a1"));
        assertFalse(policy.allows(reception, "GET", "/api/payments"));
        assertFalse(policy.allows(reception, "GET", "/api/users"));
        assertFalse(policy.allows(reception, "POST", "/api/roles"));
    }

    @Test
    @DisplayName("a custom role with no permissions can do nothing")
    void emptyRoleIsDeniedEverything() {
        Authentication nobody = signedIn("newbie", "TRAINEE", Set.of());
        for (PermissionModule m : PermissionModule.values())
            assertFalse(policy.allows(nobody, "GET", m.getApiPrefix()), m.name());
    }

    @Test
    @DisplayName("ADMIN keeps full access; CUSTOMER and STYLIST get nothing from this policy")
    void builtInRoles() {
        Authentication admin = signedIn("boss", "ADMIN", Set.of());
        Authentication customer = signedIn("cara", "CUSTOMER", Set.of("PAYMENTS:VIEW"));   // stray data ignored
        assertTrue(policy.allows(admin, "DELETE", "/api/users/u1"));
        assertFalse(policy.allows(customer, "GET", "/api/payments"));
        assertEquals(PermissionPolicy.all(), policy.permissionsOf(admin));
    }

    @Test
    @DisplayName("normalise: rejects unknown permissions and adds VIEW for any other action")
    void normaliseAddsViewAndRejectsUnknown() {
        assertEquals(Set.of("PAYMENTS:VIEW", "PAYMENTS:CREATE"), PermissionPolicy.normalise(Set.of("payments:create")));
        assertThrows(IllegalArgumentException.class, () -> PermissionPolicy.normalise(Set.of("USERS:EDIT")));
        assertThrows(IllegalArgumentException.class, () -> PermissionPolicy.normalise(Set.of("EVERYTHING")));
        assertTrue(PermissionPolicy.normalise(null).isEmpty());
    }
}
