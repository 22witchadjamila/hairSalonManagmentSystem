package za.ac.cput.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import za.ac.cput.domain.Appointment;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.service.AuthService;

/**
 * Ownership checks for method security - chapter 9.7.6's expression-based annotations, used as
 *
 *     @PreAuthorize("hasRole('ADMIN') or @access.isCustomer(#customerId)")
 *
 * A role check alone answers "is this a customer?". These answer "is this THE customer whose
 * data is being asked for?". Without them, any signed-in customer could read another customer's
 * bookings simply by changing the id in the URL.
 *
 * Identity always comes from the server-side session, never from the request.
 */
@Component("access")
public class AccessRules {

    private final AuthService authService;
    private final AppointmentRepository appointments;

    public AccessRules(AuthService authService, AppointmentRepository appointments) {
        this.authService = authService;
        this.appointments = appointments;
    }

    /** The signed-in user is a CUSTOMER and this is their own customer id. */
    public boolean isCustomer(String customerId) {
        String username = usernameWithRole(RoleSeeder.CUSTOMER);
        return username != null && customerId != null
                && authService.findCustomerFor(username)
                        .map(c -> customerId.equals(c.getCustomerId())).orElse(false);
    }

    /** The signed-in user is a STYLIST and this is their own stylist id. */
    public boolean isStylist(String stylistId) {
        String username = usernameWithRole(RoleSeeder.STYLIST);
        return username != null && stylistId != null
                && authService.findStylistFor(username)
                        .map(s -> stylistId.equals(s.getStylistId())).orElse(false);
    }

    /** The appointment was booked by the signed-in customer. */
    public boolean isAppointmentCustomer(String appointmentId) {
        return appointments.findById(appointmentId)
                .map(Appointment::getCustomer)
                .map(c -> isCustomer(c.getCustomerId()))
                .orElse(false);
    }

    /** The appointment is with the signed-in stylist. */
    public boolean isAppointmentStylist(String appointmentId) {
        return appointments.findById(appointmentId)
                .map(Appointment::getStylist)
                .map(s -> isStylist(s.getStylistId()))
                .orElse(false);
    }

    /** Either party to the appointment. */
    public boolean ownsAppointment(String appointmentId) {
        return isAppointmentCustomer(appointmentId) || isAppointmentStylist(appointmentId);
    }

    private String usernameWithRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        boolean hasRole = auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
        return hasRole ? auth.getName() : null;
    }
}
