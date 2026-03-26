/* PaymentFactoryTest.java
   Unit test for PaymentFactory
   Author: Reece Josephs (218152701)
   Date: [26 March 2026]
*/


package za.ac.cput.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Payment;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {

    @Test
    void testCreatePayment_success() {
        Payment payment = PaymentFactory.createPayment("P001", 250.00, "Credit Card");
        assertNotNull(payment);
        assertEquals("P001", payment.getPaymentId());
        assertEquals(250.00, payment.getAmount());
        assertEquals("Credit Card", payment.getPaymentMethod());
        System.out.println("Created: " + payment);
    }

    @Test
    void testCreatePayment_nullId_returnsNull() {
        Payment payment = PaymentFactory.createPayment(null, 250.00, "Cash");
        assertNull(payment);
        System.out.println("Null ID test passed: returned null");
    }

    @Test
    void testCreatePayment_emptyId_returnsNull() {
        Payment payment = PaymentFactory.createPayment("", 250.00, "Cash");
        assertNull(payment);
        System.out.println("Empty ID test passed: returned null");
    }

    @Test
    void testCreatePayment_nullPaymentMethod_returnsNull() {
        Payment payment = PaymentFactory.createPayment("P002", 100.00, null);
        assertNull(payment);
        System.out.println("Null payment method test passed: returned null");
    }

    @Test
    void testCreatePayment_invalidAmount_returnsNull() {
        Payment payment = PaymentFactory.createPayment("P003", 0, "EFT");
        assertNull(payment);
        System.out.println("Invalid amount test passed: returned null");
    }

    @org.junit.jupiter.api.Test
    void createPayment() {
    }
}