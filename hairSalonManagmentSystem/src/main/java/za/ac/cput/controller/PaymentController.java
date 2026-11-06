package za.ac.cput.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.service.IPaymentService;
import za.ac.cput.service.PaymentRecordingService;

import java.math.BigDecimal;
import java.util.List;

/** Payments. Staff only - SecurityConfig sends every /api/payments path to ROLE_ADMIN. */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("@perm.granted()")
public class PaymentController {

    private final IPaymentService service;
    private final PaymentRecordingService recording;

    public PaymentController(IPaymentService service, PaymentRecordingService recording) {
        this.service = service;
        this.recording = recording;
    }

    /** Record an in-store payment. No amount: the server prices it from the booked service. */
    public record RecordPaymentRequest(String appointmentId, PaymentMethod method, String promoCode,
                                       Integer redeemPoints) {
        public RecordPaymentRequest(String appointmentId, PaymentMethod method, String promoCode) {
            this(appointmentId, method, promoCode, null);
        }
    }

    /** Kept for older clients. The amount field is IGNORED - it used to set the price. */
    public record ProcessPaymentRequest(String appointmentId, BigDecimal amount,
                                        PaymentMethod method, String promoCode) {}

    @PostMapping("/record")
    @ResponseStatus(HttpStatus.CREATED)
    public Payment record(@RequestBody RecordPaymentRequest request) {
        return recording.recordInStorePayment(request.appointmentId(), request.method(), request.promoCode(),
                request.redeemPoints());
    }

    /**
     * Previously trusted the amount sent by the client, so a request could set any price.
     * Now routed through the same server-priced path as /record.
     */
    @PostMapping("/process")
    @ResponseStatus(HttpStatus.CREATED)
    public Payment process(@RequestBody ProcessPaymentRequest request) {
        return recording.recordInStorePayment(request.appointmentId(), request.method(), request.promoCode(), null);
    }

    @PostMapping("/{id}/refund")
    public Payment refund(@PathVariable String id) {
        return recording.refund(id);
    }

    /** Dashboard totals from the real rows: only PAID is revenue. */
    @GetMapping("/summary")
    public PaymentRecordingService.Summary summary() {
        return recording.summary();
    }

    @GetMapping("/{id}")
    public Payment read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Payment> getAll() {
        return service.getAll();
    }

    /** Latest payment for an appointment (a refunded one may sit behind a newer one). */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Payment> findByAppointment(@PathVariable String appointmentId) {
        return recording.latestFor(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        recording.delete(id);
    }
}
