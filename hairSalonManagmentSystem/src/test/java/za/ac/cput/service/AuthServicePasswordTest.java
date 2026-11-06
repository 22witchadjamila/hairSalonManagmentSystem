package za.ac.cput.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.config.RoleSeeder;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.repository.RoleRepository;
import za.ac.cput.repository.StylistRepository;
import za.ac.cput.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - changing and resetting passwords")
class AuthServicePasswordTest {

    @Mock UserRepository users;
    @Mock CustomerRepository customers;
    @Mock StylistRepository stylists;
    @Mock RoleRepository roles;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, customers, stylists, roles, encoder);
    }

    private User user(String id, String username, String role, String rawPassword) {
        return new User.Builder().setUserId(id).setUsername(username).setPassword(encoder.encode(rawPassword))
                .setEmail(Email.of(username + "@salon.test")).setFirstName("F").setLastName("L").setActive(true)
                .setRole(new Role.Builder().setRoleId("r-" + role).setName(role).build()).build();
    }

    @Test
    @DisplayName("passwordMatches() checks the raw password against the stored hash")
    void passwordMatches() {
        when(users.findByUsername("cara")).thenReturn(Optional.of(user("u1", "cara", RoleSeeder.CUSTOMER, "old-Pass-1")));
        assertTrue(service.passwordMatches("cara", "old-Pass-1"));
        assertFalse(service.passwordMatches("cara", "wrong-Pass-1"));
        assertFalse(service.passwordMatches("cara", null));
        when(users.findByUsername("nobody")).thenReturn(Optional.empty());
        assertFalse(service.passwordMatches("nobody", "anything1"));
    }

    @Test
    @DisplayName("changePassword() stores a NEW BCrypt hash and keeps everything else about the login")
    void changePassword_storesNewHash() {
        User cara = user("u1", "cara", RoleSeeder.CUSTOMER, "old-Pass-1");
        when(users.findByUsername("cara")).thenReturn(Optional.of(cara));
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);

        service.changePassword("cara", "old-Pass-1", "brand-New-2");

        verify(users).save(saved.capture());
        assertTrue(encoder.matches("brand-New-2", saved.getValue().getPassword()));
        assertNotEquals("brand-New-2", saved.getValue().getPassword(), "never stored as plain text");
        assertEquals("u1", saved.getValue().getUserId());
        assertEquals("cara", saved.getValue().getUsername());
        assertEquals(RoleSeeder.CUSTOMER, saved.getValue().getRole().getName());
        assertTrue(saved.getValue().isActive());
    }

    @Test
    @DisplayName("changePassword() refuses the same password, and a password that breaks the policy")
    void changePassword_refusals() {
        when(users.findByUsername("cara")).thenReturn(Optional.of(user("u1", "cara", RoleSeeder.CUSTOMER, "old-Pass-1")));

        assertThrows(InvalidOperationException.class, () -> service.changePassword("cara", "old-Pass-1", "old-Pass-1"));
        assertThrows(InvalidOperationException.class, () -> service.changePassword("cara", "old-Pass-1", "short1"));
        assertThrows(InvalidOperationException.class, () -> service.changePassword("cara", "old-Pass-1", "Password123"));
        assertThrows(InvalidOperationException.class, () -> service.changePassword("cara", "old-Pass-1", "cara-Pass-1"));   // contains username
        verify(users, never()).save(any());
    }

    @Test
    @DisplayName("staff need 12 characters, customers need 8")
    void minimumLengthByRole() {
        when(users.findByUsername("cara")).thenReturn(Optional.of(user("u1", "cara", RoleSeeder.CUSTOMER, "old-Pass-1")));
        when(users.findByUsername("sam")).thenReturn(Optional.of(user("u2", "sam", RoleSeeder.ADMIN, "old-Admin-Pass-1")));

        assertDoesNotThrow(() -> service.changePassword("cara", "old-Pass-1", "eight-8aa"));    // 9 chars, fine for a customer
        assertThrows(InvalidOperationException.class,
                () -> service.changePassword("sam", "old-Admin-Pass-1", "eight-8aa"));          // too short for staff
    }

    @Test
    @DisplayName("resetPassword() sets another person's password and says whose it was")
    void resetPassword_forSomeoneElse() {
        when(users.findById("u1")).thenReturn(Optional.of(user("u1", "cara", RoleSeeder.CUSTOMER, "forgotten-1")));
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);

        String who = service.resetPassword("u1", "temporary-Pass-5", "the-admin");

        assertEquals("cara", who);
        verify(users).save(saved.capture());
        assertTrue(encoder.matches("temporary-Pass-5", saved.getValue().getPassword()));
    }

    @Test
    @DisplayName("resetPassword() refuses your own account - use 'change my password', which asks for the current one")
    void resetPassword_refusesSelf() {
        when(users.findById("u2")).thenReturn(Optional.of(user("u2", "the-admin", RoleSeeder.ADMIN, "old-Admin-Pass-1")));
        assertThrows(InvalidOperationException.class, () -> service.resetPassword("u2", "another-Long-Pass-3", "the-admin"));
        verify(users, never()).save(any());
    }
}
