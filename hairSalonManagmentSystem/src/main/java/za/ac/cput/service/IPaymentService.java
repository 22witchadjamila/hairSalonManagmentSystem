package za.ac.cput.service;

import za.ac.cput.domain.Payment;
import za.ac.cput.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public interface IPaymentService extends IService<Payment, String> {
    /** Processes a payment for an appointment, optionally applying a promo code. */
    Payment processPayment(String appointmentId, BigDecimal amount, PaymentMethod method, String promoCode);
    Payment findByAppointment(String appointmentId);
}
