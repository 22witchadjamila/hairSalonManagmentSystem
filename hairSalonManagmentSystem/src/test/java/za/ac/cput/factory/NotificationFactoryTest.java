package za.ac.cput.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.*;
import za.ac.cput.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationFactory Tests")
class NotificationFactoryTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        Customer customer = CustomerFactory.buildCustomer("Jane", "Doe", "j@e.com", "0821234567");
        Stylist stylist   = StylistFactory.buildStylist("Lebo", "M", "l@s.com", "0711234567", "Cuts");
        SalonService salonService = SalonServiceFactory.buildService(
                "Blowout", "Full blowout and style", 45, new BigDecimal("150.00"), ServiceCategory.STYLING);
        appointment = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), null);
    }

    @Test
    @DisplayName("Should create appointment reminder with SCHEDULED status")
    void shouldCreateReminderWithScheduledStatus() {
        Notification notification = NotificationFactory.buildAppointmentReminder(
                appointment, NotificationChannel.SMS);
        assertNotNull(notification);
        assertEquals(NotificationType.APPOINTMENT_REMINDER, notification.getType());
        assertEquals(NotificationStatus.SCHEDULED, notification.getStatus());
        assertEquals(NotificationChannel.SMS, notification.getChannel());
        assertNotNull(notification.getMessage());
        assertNotNull(notification.getNotificationId());
    }

    @Test
    @DisplayName("Should schedule reminder for 9AM one day before appointment")
    void shouldScheduleOneDayBefore() {
        Notification notification = NotificationFactory.buildAppointmentReminder(
                appointment, NotificationChannel.EMAIL);
        assertNotNull(notification);
        assertEquals(appointment.getAppointmentDate().minusDays(1),
                notification.getScheduledAt().toLocalDate());
        assertEquals(9, notification.getScheduledAt().getHour());
    }

    @Test
    @DisplayName("Should return null when appointment is null")
    void shouldReturnNullWhenAppointmentIsNull() {
        Notification notification = NotificationFactory.buildAppointmentReminder(
                null, NotificationChannel.SMS);
        assertNull(notification);
    }

    @Test
    @DisplayName("Should return null when channel is null")
    void shouldReturnNullWhenChannelIsNull() {
        Notification notification = NotificationFactory.buildAppointmentReminder(
                appointment, null);
        assertNull(notification);
    }

    @Test
    @DisplayName("Should create promotion notification with correct type")
    void shouldCreatePromotionNotification() {
        Notification notification = NotificationFactory.buildPromotionNotification(
                "20% off all services this weekend!", NotificationChannel.SMS);
        assertNotNull(notification);
        assertEquals(NotificationType.PROMOTION, notification.getType());
        assertEquals("20% off all services this weekend!", notification.getMessage());
    }

    @Test
    @DisplayName("Should return null when promotion message is blank")
    void shouldReturnNullWhenPromotionMessageIsBlank() {
        Notification notification = NotificationFactory.buildPromotionNotification(
                "", NotificationChannel.EMAIL);
        assertNull(notification);
    }
}
