package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.service.IPaymentService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService service;

    public PaymentController(IPaymentService service) {
        this.service = service;
    }

    public record ProcessPaymentRequest(String appointmentId, BigDecimal amount,
                                        PaymentMethod method, String promoCode) {}

    @PostMapping("/process")
    public Payment process(@RequestBody ProcessPaymentRequest request) {
        return service.processPayment(request.appointmentId(), request.amount(),
                request.method(), request.promoCode());
    }

    @GetMapping("/{id}")
    public Payment read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Payment> getAll() {
        return service.getAll();
    }

    @GetMapping("/appointment/{appointmentId}")
    public Payment findByAppointment(@PathVariable String appointmentId) {
        return service.findByAppointment(appointmentId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}