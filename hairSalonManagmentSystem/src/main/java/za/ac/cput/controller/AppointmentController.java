package za.ac.cput.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.config.AccessRules;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.IAppointmentService;
import za.ac.cput.service.INotificationService;
import za.ac.cput.service.PromotionOfferService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final IAppointmentService service;
    private final AuthService authService;
    private final PromotionOfferService offers;
    private final AccessRules access;
    private final INotificationService notifications;

    public AppointmentController(IAppointmentService service, AuthService authService,
                                 PromotionOfferService offers, AccessRules access,
                                 INotificationService notifications) {
        this.service = service;
        this.authService = authService;
        this.offers = offers;
        this.access = access;
        this.notifications = notifications;
    }

    /**
     * serviceId matches the database column (service_id) and every other API. The field used to
     * be called salonServiceId; @JsonAlias keeps accepting that name so older clients still work.
     * customerId is accepted but ignored - see book().
     */
    public record BookAppointmentRequest(String customerId, String stylistId,
                                         @JsonAlias("salonServiceId") String serviceId,
                                         LocalDate date, LocalTime startTime, String notes,
                                         String promoCode) {}

    /**
     * Chapter 9.7.6 method security, and 9.7.7 obtaining the current user.
     *
     * The customerId is taken from the SESSION, not from the request body. Trusting the body
     * would let a signed-in customer post someone else's id and book on their behalf.
     *
     * An optional promo code is validated BEFORE the booking is made (so a bad code never leaves
     * a half-made booking) and stored on the appointment as intent. The discount itself is
     * calculated when the in-store payment is recorded - see PromotionOfferService.
     */
    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional
    public Appointment book(@RequestBody BookAppointmentRequest request, Authentication authentication) {
        String customerId = authService.customerFor(authentication.getName()).getCustomerId();
        boolean hasCode = request.promoCode() != null && !request.promoCode().isBlank();
        Promotion promo = hasCode ? offers.requireBookable(request.promoCode()) : null;

        Appointment booked = service.bookAppointment(customerId, request.stylistId(), request.serviceId(),
                request.date(), request.startTime(), request.notes());
        return promo == null ? booked : offers.attachToAppointment(booked, promo);
    }

    /** Either party to the appointment, or an admin. */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.granted() or @access.ownsAppointment(#id)")
    public Appointment read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    @PreAuthorize("@perm.granted()")
    public List<Appointment> getAll() {
        return service.getAll();
    }

    /** A customer sees only their own bookings - changing the id in the URL gets 403. */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@perm.granted() or @access.isCustomer(#customerId)")
    public List<Appointment> findByCustomer(@PathVariable String customerId) {
        return service.findByCustomer(customerId);
    }

    /** A stylist sees only their own schedule. */
    @GetMapping("/stylist/{stylistId}")
    @PreAuthorize("@perm.granted() or @access.isStylist(#stylistId)")
    public List<Appointment> findByStylist(@PathVariable String stylistId) {
        return service.findByStylist(stylistId);
    }

    /** The stylist the appointment is with, or an admin. */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("@perm.granted() or @access.isAppointmentStylist(#id)")
    public Appointment confirm(@PathVariable String id) {
        return service.confirmAppointment(id);
    }

    /**
     * The customer who booked it, the stylist it is with, or an admin. When anyone OTHER than the
     * customer cancels, the customer is told in their notification bell - otherwise they would
     * only find out by looking at their bookings.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("@perm.granted() or @access.isAppointmentCustomer(#id) or @access.isAppointmentStylist(#id)")
    public Appointment cancel(@PathVariable String id) {
        boolean cancelledByCustomer = access.isAppointmentCustomer(id);
        Appointment cancelled = service.cancelAppointment(id);
        if (!cancelledByCustomer) notifications.notifyAppointmentCancelled(cancelled);
        return cancelled;
    }

    /** The stylist who did the work, or an admin. */
    @PostMapping("/{id}/complete")
    @PreAuthorize("@perm.granted() or @access.isAppointmentStylist(#id)")
    public Appointment complete(@PathVariable String id) {
        return service.completeAppointment(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.granted()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
