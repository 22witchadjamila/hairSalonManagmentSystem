/* PaymentRepositoryTest.java
   Unit test for PaymentRepository
   Author: Reece Josephs (218152701)
   Date: [26 March 2026]
*/
package za.ac.cput.repository.payment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Payment;
import za.ac.cput.factory.PaymentFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRepositoryTest {

    private PaymentRepository repository;
    private Payment payment;

    @BeforeEach
    void setUp() {
        repository = PaymentRepository.getRepository();
        repository.getAll().clear();

        payment = PaymentFactory.createPayment("P001", 250.00, "Credit Card");
    }

    @Test
    void testCreate() {
        Payment created = repository.create(payment);
        assertNotNull(created);
        assertEquals(payment.getPaymentId(), created.getPaymentId());
        System.out.println("Created: " + created);
    }

    @Test
    void testRead() {
        repository.create(payment);
        Payment read = repository.read("P001");
        assertNotNull(read);
        assertEquals("Credit Card", read.getPaymentMethod());
        System.out.println("Read: " + read);
    }

    @Test
    void testUpdate() {
        repository.create(payment);

        Payment updatedPayment = new Payment.Builder()
                .setPaymentId("P001")
                .setAmount(350.00)
                .setPaymentMethod("EFT")
                .build();

        Payment result = repository.update(updatedPayment);
        assertNotNull(result);
        assertEquals(350.00, result.getAmount());
        assertEquals("EFT", result.getPaymentMethod());
        System.out.println("Updated: " + result);
    }

    @Test
    void testDelete() {
        repository.create(payment);
        boolean deleted = repository.delete("P001");
        assertTrue(deleted);
        assertNull(repository.read("P001"));
        System.out.println("Deleted: " + deleted);
    }

    @Test
    void testGetAll() {
        repository.create(payment);
        Set<Payment> all = repository.getAll();
        assertEquals(1, all.size());
        assertTrue(all.contains(payment));
        System.out.println("All Payments: " + all);
    }
}
