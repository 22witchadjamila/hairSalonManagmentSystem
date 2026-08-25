package za.ac.cput.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.*;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackFactory Tests")
public class FeedbackFactoryTest {

    private Appointment completedAppointment;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = CustomerFactory.buildCustomer("Jane", "Doe", "j@e.com", "0821234567");
        Stylist stylist = StylistFactory.buildStylist("Lebo", "M", "l@s.com", "0711234567", "Cuts");
        SalonService salonService = SalonServiceFactory.buildService(
                "Blowout", "Full blowout and style", 45, new BigDecimal("150.00"), ServiceCategory.STYLING);
        completedAppointment = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), null);
        // manually set to COMPLETED using builder copy
        completedAppointment = new Appointment.Builder()
                .setAppointmentId(completedAppointment.getAppointmentId())
                .setCustomer(completedAppointment.getCustomer())
                .setStylist(completedAppointment.getStylist())
                .setSalonService(completedAppointment.getSalonService())
                .setAppointmentDate(completedAppointment.getAppointmentDate())
                .setTimeSlot(completedAppointment.getTimeSlot())
                .setStatus(AppointmentStatus.COMPLETED)
                .setCreatedAt(completedAppointment.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("Should create review for a completed appointment")
    void shouldCreateReviewForCompletedAppointment() {
        Feedback review = FeedbackFactory.buildFeedback(
                completedAppointment, customer, 5, "Amazing service!");
        assertNotNull(review);
        assertEquals(5, review.getRating());
        assertEquals("Amazing service!", review.getComment());
        assertNotNull(review.getSubmittedAt());
        assertFalse(review.isVerified());
    }

    @Test
    @DisplayName("Should return null when appointment is not COMPLETED")
    void shouldReturnNullWhenAppointmentNotCompleted() {
        Customer c = CustomerFactory.buildCustomer("A", "B", "a@b.com", "0811234567");
        Stylist s  = StylistFactory.buildStylist("C", "D", "c@d.com", "0711234567", "Cuts");
        SalonService salonService = SalonServiceFactory.buildService(
                "Cut", "Simple cut", 30, new BigDecimal("100.00"), ServiceCategory.HAIRCUT);
        Appointment pending = AppointmentFactory.buildAppointment(
                c, s, salonService, LocalDate.now().plusDays(2), LocalTime.of(10, 0), null);

        Feedback review = FeedbackFactory.buildFeedback(pending, c, 4, "Great");
        assertNull(review);
    }

    @Test
    @DisplayName("Should return null when rating is below 1")
    void shouldReturnNullWhenRatingBelowOne() {
        Feedback review = FeedbackFactory.buildFeedback(
                completedAppointment, customer, 0, "Too low");
        assertNull(review);
    }

    @Test
    @DisplayName("Should return null when rating is above 5")
    void shouldReturnNullWhenRatingAboveFive() {
        Feedback review = FeedbackFactory.buildFeedback(
                completedAppointment, customer, 6, "Too high");
        assertNull(review);
    }

    @Test
    @DisplayName("Should return null when customer is null")
    void shouldReturnNullWhenCustomerIsNull() {
        Feedback review = FeedbackFactory.buildFeedback(
                completedAppointment, null, 4, "Good");
        assertNull(review);
    }
}
