package za.ac.cput.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.PromotionOfferService;
import za.ac.cput.service.IAppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController Tests")
public class AppointmentControllerTest {

    @Mock
    private IAppointmentService service;

    @Mock
    private AuthService authService;

    @Mock
    private PromotionOfferService offers;
    @Mock
    private za.ac.cput.config.AccessRules access;
    @Mock
    private za.ac.cput.service.INotificationService notifications;
    @InjectMocks
    private AppointmentController controller;

    @Test
    @DisplayName("book() books for the SIGNED-IN customer, ignoring the id in the request body")
    void book_usesSessionIdentityNotRequestBody(){
        Appointment appointment = mock(Appointment.class);
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        // The session says the caller is "marc", whose customer row is c1.
        Customer sessionCustomer = mock(Customer.class);
        when(sessionCustomer.getCustomerId()).thenReturn("c1");
        when(authService.customerFor("marc")).thenReturn(sessionCustomer);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("marc");

        when(service.bookAppointment("c1", "s1", "svc1", date, time,
                "first visit")).thenReturn(appointment);

        // The body claims a DIFFERENT customer - someone trying to book on another
        // person's account. It must be ignored in favour of the session identity.
        AppointmentController.BookAppointmentRequest request =
                new AppointmentController.BookAppointmentRequest("someone-else", "s1",
                        "svc1", date, time, "first visit", null);

        assertEquals(appointment, controller.book(request, authentication));
        verify(service).bookAppointment("c1", "s1", "svc1", date, time, "first visit");
        verify(service, never()).bookAppointment(eq("someone-else"), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("read() delegates to service.read()")
    void read_delegatesToService(){
        Appointment appointment = mock(Appointment.class);
        when(service.read("a1")).thenReturn(appointment);

        assertEquals(appointment, controller.read("a1"));
    }

    @Test
    @DisplayName("getAll() delegates to service.getAll()")
    void getAll_delegatesToService(){
        List<Appointment> appointments = List.of(mock(Appointment.class));
        when(service.getAll()).thenReturn(appointments);

        assertEquals(appointments, controller.getAll());
    }

    @Test
    @DisplayName("findByCustomer() delegates to service.findByCustomer()")
    void findByCustomer_delegatesToService(){
        List<Appointment> appointments = List.of(mock(Appointment.class));
        when(service.findByCustomer("c1")).thenReturn(appointments);

        assertEquals(appointments, controller.findByCustomer("c1"));
    }

    @Test
    @DisplayName("findByStylist() delegates to service.findByStylist()")
    void findByStylist_delegatesToService(){
        List<Appointment> appointments = List.of(mock(Appointment.class));
        when(service.findByStylist("s1")).thenReturn(appointments);

        assertEquals(appointments, controller.findByStylist("s1"));
    }

    @Test
    @DisplayName("confirm() delegates to service.confirmAppointment()")
    void confirm_delegatesToService(){
        Appointment appointment = mock(Appointment.class);
        when(service.confirmAppointment("a1")).thenReturn(appointment);

        assertEquals(appointment, controller.confirm("a1"));
    }

    @Test
    @DisplayName("cancel() delegates to service.cancelAppointment()")
    void cancel_delegatesToService(){
        Appointment appointment = mock(Appointment.class);
        when(service.cancelAppointment("a1")).thenReturn(appointment);

        assertEquals(appointment, controller.cancel("a1"));
    }

    @Test
    @DisplayName("complete() delegates to service.completeAppointment()")
    void complete_delegatesToService(){
        Appointment appointment = mock(Appointment.class);
        when(service.completeAppointment("a1")).thenReturn(appointment);

        assertEquals(appointment, controller.complete("a1"));
    }

    @Test
    @DisplayName("delete() delegates to service.delete()")
    void delete_delegatesToService(){
        controller.delete("a1");

        verify(service).delete("a1");
    }

    @Test
    @DisplayName("cancel() by someone OTHER than the customer (stylist or admin) tells the customer")
    void cancel_byStaff_notifiesCustomer() {
        Appointment appointment = mock(Appointment.class);
        when(service.cancelAppointment("a1")).thenReturn(appointment);
        when(access.isAppointmentCustomer("a1")).thenReturn(false);

        controller.cancel("a1");

        verify(notifications).notifyAppointmentCancelled(appointment);
    }

    @Test
    @DisplayName("cancel() by the customer themself sends no notification - they just did it")
    void cancel_byCustomer_doesNotNotify() {
        Appointment appointment = mock(Appointment.class);
        when(service.cancelAppointment("a1")).thenReturn(appointment);
        when(access.isAppointmentCustomer("a1")).thenReturn(true);

        controller.cancel("a1");

        verify(notifications, never()).notifyAppointmentCancelled(any());
    }

    @Test
    @DisplayName("cancel() that the service refuses sends no notification")
    void cancel_refused_doesNotNotify() {
        when(service.cancelAppointment("a1")).thenThrow(new za.ac.cput.exception.InvalidOperationException("no"));

        org.junit.jupiter.api.Assertions.assertThrows(za.ac.cput.exception.InvalidOperationException.class, () -> controller.cancel("a1"));
        verify(notifications, never()).notifyAppointmentCancelled(any());
    }
}
