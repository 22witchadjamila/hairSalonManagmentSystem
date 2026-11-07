package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.RoleRepository;
import za.ac.cput.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAdminService - custom roles without privilege escalation")
class RoleAdminServiceTest {

    @Mock RoleRepository roles;
    @Mock UserRepository users;
    @InjectMocks RoleAdminService service;

    private static Role role(String id, String name) {
        return new Role.Builder().setRoleId(id).setName(name).build();
    }

    private static User user(String id, String username, Role role) {
        return new User.Builder().setUserId(id).setUsername(username).setRole(role).setActive(true).build();
    }

    @Test
    @DisplayName("a new role starts with NO permissions unless some are chosen")
    void newRoleDefaultsToNothing() {
        when(roles.findByName("RECEPTION")).thenReturn(Optional.empty());
        when(roles.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role created = service.create("reception", "Front desk", null);

        assertEquals("RECEPTION", created.getName());
        assertTrue(created.getPermissions().isEmpty());
    }

    @Test
    @DisplayName("chosen permissions are stored (and VIEW is implied by EDIT)")
    void storesPermissions() {
        when(roles.findByName("RECEPTION")).thenReturn(Optional.empty());
        when(roles.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role created = service.create("Reception", null, Set.of("APPOINTMENTS:EDIT"));

        assertEquals(Set.of("APPOINTMENTS:VIEW", "APPOINTMENTS:EDIT"), created.getPermissions());
    }

    @Test
    @DisplayName("refuses a permission that would manage users or roles")
    void refusesUserAdministrationPermission() {
        when(roles.findByName("SNEAKY")).thenReturn(Optional.empty());
        assertThrows(InvalidOperationException.class, () -> service.create("SNEAKY", null, Set.of("USERS:EDIT")));
        verify(roles, never()).save(any());
    }

    @Test
    @DisplayName("built-in names can't be re-created, and built-in roles can't be edited or deleted")
    void builtInsAreProtected() {
        assertThrows(InvalidOperationException.class, () -> service.create("admin", null, Set.of()));
        when(roles.findById("r-admin")).thenReturn(Optional.of(role("r-admin", "ADMIN")));
        assertThrows(InvalidOperationException.class, () -> service.update("r-admin", "x", Set.of("PAYMENTS:VIEW")));
        assertThrows(InvalidOperationException.class, () -> service.delete("r-admin"));
    }

    @Test
    @DisplayName("a role still given to logins can't be deleted")
    void inUseRoleCantBeDeleted() {
        Role reception = role("r1", "RECEPTION");
        when(roles.findById("r1")).thenReturn(Optional.of(reception));
        when(users.findByRole_Name("RECEPTION")).thenReturn(List.of(user("u1", "rita", reception)));
        assertThrows(InvalidOperationException.class, () -> service.delete("r1"));
        verify(roles, never()).delete(any());
    }

    @Test
    @DisplayName("assignRole: can't change your own role, or demote the last admin")
    void assignRoleGuards() {
        Role admin = role("r-admin", "ADMIN");
        Role reception = role("r1", "RECEPTION");
        when(roles.findById("r1")).thenReturn(Optional.of(reception));
        when(users.findById("u-boss")).thenReturn(Optional.of(user("u-boss", "boss", admin)));

        assertThrows(InvalidOperationException.class, () -> service.assignRole("u-boss", "r1", "boss"));

        when(users.findByRole_Name("ADMIN")).thenReturn(List.of(user("u-boss", "boss", admin)));
        assertThrows(InvalidOperationException.class, () -> service.assignRole("u-boss", "r1", "someone-else"));
    }

    @Test
    @DisplayName("assignRole: customer and stylist logins keep their role")
    void customerLoginsKeepTheirRole() {
        Role customer = role("r-c", "CUSTOMER");
        when(roles.findById("r1")).thenReturn(Optional.of(role("r1", "RECEPTION")));
        when(users.findById("u-c")).thenReturn(Optional.of(user("u-c", "cara", customer)));
        assertThrows(InvalidOperationException.class, () -> service.assignRole("u-c", "r1", "boss"));
    }

    @Test
    @DisplayName("assignRole: moves a staff login to a custom role")
    void movesStaffLogin() {
        Role admin = role("r-admin", "ADMIN");
        Role reception = role("r1", "RECEPTION");
        when(roles.findById("r1")).thenReturn(Optional.of(reception));
        when(users.findById("u2")).thenReturn(Optional.of(user("u2", "second-admin", admin)));
        when(users.findByRole_Name("ADMIN")).thenReturn(List.of(user("u1", "boss", admin), user("u2", "second-admin", admin)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("RECEPTION", service.assignRole("u2", "r1", "boss").getRole().getName());
    }
}
