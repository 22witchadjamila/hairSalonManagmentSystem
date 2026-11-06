package za.ac.cput.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.service.IPaymentService;
import za.ac.cput.service.PaymentRecordingService;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Mock
    private IPaymentService service;

    @Mock
    private PaymentRecordingService recording;

    @InjectMocks
    private PaymentController controller;

    @Test
    @DisplayName("process() ignores the client's amount - the server prices the payment")
    void process_ignoresClientAmount() {
        Payment payment = mock(Payment.class);
        when(recording.recordInStorePayment("a1", PaymentMethod.CARD, "WELCOME10", null)).thenReturn(payment);

        // A client trying to pay R1 for a R150 service: the amount must not reach the pricing logic.
        PaymentController.ProcessPaymentRequest request =
                new PaymentController.ProcessPaymentRequest("a1", new BigDecimal("1.00"), PaymentMethod.CARD, "WELCOME10");

        assertEquals(payment, controller.process(request));
        verify(service, never()).processPayment(any(), any(), any(), any());
    }

    @Test
    @DisplayName("record() records an in-store payment")
    void record_delegatesToRecordingService() {
        Payment payment = mock(Payment.class);
        when(recording.recordInStorePayment("a1", PaymentMethod.CASH, null, null)).thenReturn(payment);

        assertEquals(payment, controller.record(new PaymentController.RecordPaymentRequest("a1", PaymentMethod.CASH, null)));
    }

    @Test
    @DisplayName("read() delegates to service.read()")
    void read_delegatesToService() {
        Payment payment = mock(Payment.class);
        when(service.read("p1")).thenReturn(payment);

        assertEquals(payment, controller.read("p1"));
    }

    @Test
    @DisplayName("getAll() delegates to service.getAll()")
    void getAll_delegatesToService() {
        List<Payment> payments = List.of(mock(Payment.class));
        when(service.getAll()).thenReturn(payments);

        assertEquals(payments, controller.getAll());
    }

    @Test
    @DisplayName("findByAppointment() returns the latest payment, or 404")
    void findByAppointment_returnsLatestOr404() {
        Payment payment = mock(Payment.class);
        when(recording.latestFor("a1")).thenReturn(Optional.of(payment));
        when(recording.latestFor("none")).thenReturn(Optional.empty());

        assertEquals(payment, controller.findByAppointment("a1").getBody());
        assertEquals(404, controller.findByAppointment("none").getStatusCode().value());
    }

    @Test
    @DisplayName("delete() goes through the recording service, which refuses PAID payments")
    void delete_delegatesToRecordingService() {
        controller.delete("p1");

        verify(recording).delete("p1");
    }
}

