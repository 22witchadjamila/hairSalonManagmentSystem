/* PaymentRepository.java
   Repository implementation class for Payment
   Author: Reece Josephs (218152701)
   Date: [26 March 2026]
*/


package za.ac.cput.repository.payment.impl;

import za.ac.cput.domain.Payment;
import za.ac.cput.repository.payment.IPaymentRepository;

import java.util.HashSet;
import java.util.Set;

public class PaymentRepository implements IPaymentRepository {

    private static PaymentRepository repository = null;
    private Set<Payment> paymentDB;

    private PaymentRepository() {
        paymentDB = new HashSet<>();
    }

    public static PaymentRepository getRepository() {
        if (repository == null) {
            repository = new PaymentRepository();
        }
        return repository;
    }

    @Override
    public Payment create(Payment payment) {
        paymentDB.add(payment);
        return payment;
    }

    @Override
    public Payment read(String id) {
        return paymentDB.stream()
                .filter(p -> p.getPaymentId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Payment update(Payment payment) {
        Payment old = read(payment.getPaymentId());
        if (old != null) {
            paymentDB.remove(old);
            paymentDB.add(payment);
            return payment;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Payment payment = read(id);
        if (payment != null) {
            paymentDB.remove(payment);
            return true;
        }
        return false;
    }

    @Override
    public Set<Payment> getAll() {
        return paymentDB;
    }
}