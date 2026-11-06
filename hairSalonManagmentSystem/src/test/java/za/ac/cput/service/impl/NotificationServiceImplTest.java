package za.ac.cput.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.AppointmentFactory;
import za.ac.cput.factory.CustomerFactory;
import za.ac.cput.factory.SalonServiceFactory;
import za.ac.cput.factory.StylistFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.NotificationRepository;
import za.ac.cput.service.impl.NotificationServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import za.ac.cput.domain.enums.NotificationStatus;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private NotificationServiceImpl service;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        Customer customer = CustomerFactory.buildCustomer("Jane", "Doe", "jane@example.com", "0821234567");
        Stylist stylist = StylistFactory.buildStylist("Lebo", "Mokoena", "lebo@salon.com", "0711234567", "Colouring");
        SalonService salonService = SalonServiceFactory.buildService(
                "Blowout", "Full blowout and style", 60, new BigDecimal("200.00"), ServiceCategory.STYLING);
        appointment = AppointmentFactory.buildAppointment(
                customer, stylist, salonService, LocalDate.now().plusDays(1), LocalTime.of(10, 0), null);
    }

    @Test
    @DisplayName("scheduleAppointmentReminder() builds and saves a reminder for a real appointment")
    void scheduleAppointmentReminder_savesReminder() {
        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = service.scheduleAppointmentReminder(appointment.getAppointmentId(), NotificationChannel.SMS);

        assertNotNull(result);
        assertEquals(NotificationType.APPOINTMENT_REMINDER, result.getType());
        assertEquals(NotificationChannel.SMS, result.getChannel());
    }

    @Test
    @DisplayName("scheduleAppointmentReminder() throws ResourceNotFoundException when the appointment doesn't exist")
    void scheduleAppointmentReminder_throws_whenAppointmentMissing() {
        when(appointmentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.scheduleAppointmentReminder("missing", NotificationChannel.SMS));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("sendPromotionNotification() builds and saves a promotion notification")
    void sendPromotionNotification_savesNotification() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = service.sendPromotionNotification("20% off this weekend!", NotificationChannel.IN_APP);

        assertNotNull(result);
        assertEquals(NotificationType.PROMOTION, result.getType());
        assertEquals("20% off this weekend!", result.getMessage());
    }

    @Test
    @DisplayName("sendPromotionNotification() throws InvalidOperationException for a blank message")
    void sendPromotionNotification_throws_whenMessageBlank() {
        assertThrows(InvalidOperationException.class,
                () -> service.sendPromotionNotification("", NotificationChannel.IN_APP));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("read() returns the notification when found")
    void read_returnsNotification_whenFound() {
        Notification notification = za.ac.cput.factory.NotificationFactory.buildAppointmentReminder(
                appointment, NotificationChannel.SMS);
        when(repository.findById(notification.getNotificationId())).thenReturn(Optional.of(notification));

        assertEquals(notification, service.read(notification.getNotificationId()));
    }

    @Test
    @DisplayName("getAll() returns every notification")
    void getAll_returnsAllNotifications() {
        Notification notification = za.ac.cput.factory.NotificationFactory.buildAppointmentReminder(
                appointment, NotificationChannel.SMS);
        when(repository.findAll()).thenReturn(List.of(notification));

        assertEquals(1, service.getAll().size());
    }

    @Test
    @DisplayName("sendPromotionNotification() is SENT straight away (it is visible at once, not 'scheduled')")
    void sendPromotionNotification_isSentImmediately() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = service.sendPromotionNotification("  Weekend special  ", null);   // null channel = in-app

        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertNotNull(result.getSentAt());
        assertEquals(NotificationChannel.IN_APP, result.getChannel());
        assertEquals("Weekend special", result.getMessage());
        assertNull(result.getAppointment());   // no appointment = an announcement for every customer
    }

    @Test
    @DisplayName("sendPromotionNotification() refuses Email, SMS and push - nothing sends them")
    void sendPromotionNotification_refusesUnconnectedChannels() {
        for (NotificationChannel c : new NotificationChannel[]{NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH}) {
            assertThrows(InvalidOperationException.class, () -> service.sendPromotionNotification("Hi", c), c.name());
        }
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("sendPromotionNotification() refuses a message over 280 characters")
    void sendPromotionNotification_refusesLongMessage() {
        assertThrows(InvalidOperationException.class,
                () -> service.sendPromotionNotification("x".repeat(281), NotificationChannel.IN_APP));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("findByCustomer() asks for the customer's own messages plus recent SENT promotion announcements")
    void findByCustomer_includesRecentAnnouncements() {
        service.findByCustomer("c1");

        verify(repository).findVisibleTo(eq("c1"), eq(NotificationType.PROMOTION), eq(NotificationStatus.SENT),
                argThat(since -> since.isAfter(LocalDateTime.now().minusDays(31)) && since.isBefore(LocalDateTime.now().minusDays(29))));
    }

    // ------------------------------------------------------------------ reminders

    private Appointment confirmedAt(LocalDate date, LocalTime time) {
        Customer customer = appointment.getCustomer();
        return new Appointment.Builder().setAppointmentId("a-" + date + time).setCustomer(customer)
                .setStylist(appointment.getStylist()).setSalonService(appointment.getSalonService())
                .setAppointmentDate(date).setTimeSlot(za.ac.cput.domain.valueobject.TimeSlot.of(time, time.plusHours(1)))
                .setStatus(za.ac.cput.domain.enums.AppointmentStatus.CONFIRMED).build();
    }

    @Test
    @DisplayName("sendDueReminders() creates one in-app reminder for a confirmed appointment tomorrow")
    void sendDueReminders_createsForTomorrow() {
        LocalDateTime now = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Appointment tomorrow = confirmedAt(now.toLocalDate().plusDays(1), LocalTime.of(10, 0));
        when(appointmentRepository.findByAppointmentDateBetweenAndStatus(any(), any(), any())).thenReturn(List.of(tomorrow));
        when(repository.existsByAppointment_AppointmentIdAndType(any(), any())).thenReturn(false);

        int created = service.sendDueReminders(now);

        assertEquals(1, created);
        org.mockito.ArgumentCaptor<Notification> captor = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification n = captor.getValue();
        assertEquals(NotificationType.APPOINTMENT_REMINDER, n.getType());
        assertEquals(NotificationChannel.IN_APP, n.getChannel());
        assertEquals(NotificationStatus.SENT, n.getStatus());
        assertSame(tomorrow, n.getAppointment());
        assertTrue(n.getMessage().contains("tomorrow") && n.getMessage().contains("Blowout") && n.getMessage().contains("Lebo"), n.getMessage());
    }

    @Test
    @DisplayName("sendDueReminders() says 'today' for a same-day appointment that is still ahead")
    void sendDueReminders_sameDay() {
        LocalDateTime now = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        when(appointmentRepository.findByAppointmentDateBetweenAndStatus(any(), any(), any()))
                .thenReturn(List.of(confirmedAt(now.toLocalDate(), LocalTime.of(15, 0))));
        when(repository.existsByAppointment_AppointmentIdAndType(any(), any())).thenReturn(false);

        assertEquals(1, service.sendDueReminders(now));
        verify(repository).save(argThat(n -> n.getMessage().contains("today")));
    }

    @Test
    @DisplayName("sendDueReminders() skips an appointment already reminded, and one that has already started")
    void sendDueReminders_skipsRemindedAndPast() {
        LocalDateTime now = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0);
        Appointment already = confirmedAt(now.toLocalDate().plusDays(1), LocalTime.of(10, 0));
        Appointment past = confirmedAt(now.toLocalDate(), LocalTime.of(9, 0));
        when(appointmentRepository.findByAppointmentDateBetweenAndStatus(any(), any(), any())).thenReturn(List.of(already, past));
        when(repository.existsByAppointment_AppointmentIdAndType(already.getAppointmentId(), NotificationType.APPOINTMENT_REMINDER)).thenReturn(true);

        assertEquals(0, service.sendDueReminders(now));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("sendDueReminders() only looks at CONFIRMED appointments, today and tomorrow")
    void sendDueReminders_queryWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 9, 0);
        service.sendDueReminders(now);
        verify(appointmentRepository).findByAppointmentDateBetweenAndStatus(
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 4), za.ac.cput.domain.enums.AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("notifyAppointmentCancelled() saves a delivered in-app message that says it was cancelled")
    void notifyAppointmentCancelled_savesMessage() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification n = service.notifyAppointmentCancelled(appointment);

        assertEquals(NotificationChannel.IN_APP, n.getChannel());
        assertEquals(NotificationStatus.SENT, n.getStatus());
        assertSame(appointment, n.getAppointment());
        assertTrue(n.getMessage().contains("cancelled") && n.getMessage().contains("Blowout"), n.getMessage());
        // GENERAL, not a new enum value: the MySQL enum column wouldn't accept one.
        assertEquals(NotificationType.GENERAL, n.getType());
    }
}
