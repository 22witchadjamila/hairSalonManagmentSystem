package za.ac.cput.service.impl;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.PaymentFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.service.IPaymentService;
import za.ac.cput.service.IPromotionService;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.util.List;

public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final IPromotionService promotionService;

    public PaymentServiceImpl(PaymentRepository repository,
                              AppointmentRepository appointmentRepository,
                              IPromotionService promotionService) {
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
        this.promotionService = promotionService;
    }

    @Override
    public Payment create(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Payment read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Payment", id));
    }

    @Override
    public Payment update(Payment payment) {
        read(payment.getPaymentId());
        return repository.save(payment);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Payment> getAll() {
        return repository.findAll();
    }

    @Override
    public Payment processPayment(String appointmentId, BigDecimal amount, PaymentMethod method, String promoCode) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Appointment", appointmentId));

        Payment payment;
        if (Helper.isNullOrEmpty(promoCode)) {
            payment = PaymentFactory.buildPayment(appointment, amount, method);
        } else {
            Promotion promotion = promotionService.validate(promoCode);
            payment = PaymentFactory.buildPaymentWithPromotion(appointment, amount, method, promotion);
            promotionService.recordUsage(promotion);
        }

        if (payment == null) {
            throw new InvalidOperationException("Invalid payment details provided.");
        }
        return create(payment);
    }

    @Override
    public Payment findByAppointment(String appointmentId) {
        return repository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for appointment: " + appointmentId));
    }

}
