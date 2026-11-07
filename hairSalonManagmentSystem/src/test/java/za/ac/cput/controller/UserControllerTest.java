package za.ac.cput.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.User;
import za.ac.cput.service.AuthService;
import org.springframework.security.core.Authentication;
import za.ac.cput.service.IUserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * UserController now creates ADMIN logins through AuthService (with a hashed password) and
 * deletes through AuthService's guarded path, so those two collaborators are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private IUserService service;

    @Mock
    private AuthService authService;

    @Mock
    private za.ac.cput.service.RoleAdminService roleAdmin;

    @InjectMocks
    private UserController controller;

    @Test
    @DisplayName("create() makes an admin login WITH a password - no more password-less accounts")
    void create_createsAdminWithPassword() {
        User user = mock(User.class);
        when(authService.createAdmin("jdoe", "a-long-password", "j@email.com", "Jane", "Doe")).thenReturn(user);

        User result = controller.create(new UserController.AdminUserRequest(
                "jdoe", "a-long-password", "j@email.com", "Jane", "Doe", null));

        assertEquals(user, result);
        verify(service, never()).register(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("read() delegates to service.read()")
    void read_delegatesToService() {
        User user = mock(User.class);
        when(service.read("u1")).thenReturn(user);

        assertEquals(user, controller.read("u1"));
    }

    @Test
    @DisplayName("getAll() delegates to service.getAll()")
    void getAll_delegatesToService() {
        List<User> users = List.of(mock(User.class));
        when(service.getAll()).thenReturn(users);

        assertEquals(users, controller.getAll());
    }

    @Test
    @DisplayName("findByUsername() delegates to service.findByUsername()")
    void findByUsername_delegatesToService() {
        User user = mock(User.class);
        when(service.findByUsername("jdoe")).thenReturn(user);

        assertEquals(user, controller.findByUsername("jdoe"));
    }

    @Test
    @DisplayName("delete() goes through the guarded path, passing who is asking")
    void delete_usesGuardedDelete() {
        Authentication me = mock(Authentication.class);
        when(me.getName()).thenReturn("judith");

        controller.delete("u1", me);

        verify(authService).deleteLogin("u1", "judith");
        verify(service, never()).delete(any());
    }
}

