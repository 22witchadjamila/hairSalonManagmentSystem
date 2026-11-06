package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import za.ac.cput.config.RoleSeeder;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.User;
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The admin delete fixes: services archive instead of failing, logins delete safely. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Admin delete strategies")
class RetirementAndLoginDeletionTest {

    @Mock SalonServiceRepository services;
    @Mock StylistRepository stylists;
    @Mock AppointmentRepository appointments;
    @Mock ProductInventoryRepository products;
    @Mock UserRepository users;
    @Mock CustomerRepository customers;
    @Mock RoleRepository roles;

    private SalonService cut() {
        return new SalonService.Builder().setServiceId("svc1").setName("Classic Haircut")
                .setPrice(Money.of(new BigDecimal("160"))).setDurationMinutes(35).setActive(true).build();
    }

    @Test
    @DisplayName("a service with bookings is ARCHIVED, not hard-deleted (the FK would refuse it)")
    void bookedServiceIsArchived() {
        RetirementService retirement = new RetirementService(services, stylists, appointments, products);
        when(services.findById("svc1")).thenReturn(Optional.of(cut()));
        when(appointments.existsBySalonService_ServiceId("svc1")).thenReturn(true);

        RetirementService.Result result = retirement.retireService("svc1");

        assertEquals(RetirementService.Outcome.ARCHIVED, result.outcome());
        ArgumentCaptor<SalonService> saved = ArgumentCaptor.forClass(SalonService.class);
        verify(services).save(saved.capture());
        assertFalse(saved.getValue().isActive(), "archived = inactive");
        verify(services, never()).delete(any());
    }

    @Test
    @DisplayName("an unused service is deleted outright")
    void unusedServiceIsDeleted() {
        RetirementService retirement = new RetirementService(services, stylists, appointments, products);
        when(services.findById("svc1")).thenReturn(Optional.of(cut()));

        assertEquals(RetirementService.Outcome.DELETED, retirement.retireService("svc1").outcome());
        verify(services).delete(any());
    }

    private AuthService auth() {
        return new AuthService(users, customers, stylists, roles, new BCryptPasswordEncoder());
    }

    private User admin(String id, String username) {
        return new User.Builder().setUserId(id).setUsername(username)
                .setRole(new Role.Builder().setRoleId("r").setName(RoleSeeder.ADMIN).build()).build();
    }

    @Test
    @DisplayName("an admin cannot delete the account they are signed in with")
    void cannotDeleteYourself() {
        when(users.findById("u1")).thenReturn(Optional.of(admin("u1", "judith")));
        assertThrows(InvalidOperationException.class, () -> auth().deleteLogin("u1", "judith"));
        verify(users, never()).delete(any());
    }

    @Test
    @DisplayName("the last admin cannot be deleted")
    void cannotDeleteLastAdmin() {
        User only = admin("u2", "other");
        when(users.findById("u2")).thenReturn(Optional.of(only));
        when(users.findByRole_Name(RoleSeeder.ADMIN)).thenReturn(List.of(only));
        assertThrows(InvalidOperationException.class, () -> auth().deleteLogin("u2", "judith"));
        verify(users, never()).delete(any());
    }

    @Test
    @DisplayName("another admin can be deleted when one remains")
    void deletesOtherAdmin() {
        User other = admin("u2", "other");
        when(users.findById("u2")).thenReturn(Optional.of(other));
        when(users.findByRole_Name(RoleSeeder.ADMIN)).thenReturn(List.of(other, admin("u1", "judith")));
        auth().deleteLogin("u2", "judith");
        verify(users).delete(other);
    }
}
