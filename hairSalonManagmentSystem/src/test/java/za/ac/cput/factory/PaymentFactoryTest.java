/* PaymentFactoryTest.java
   Unit test for PaymentFactory
   Author: Reece Josephs (218152701)
   Date: [26 March 2026]
*/


package za.ac.cput.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.*;
import za.ac.cput.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentFactory Tests")
class PaymentFactoryTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        Customer customer = CustomerFactory.buildCustomer("Jane", "Doe", "j@e.com", "082");
        Stylist stylist   = StylistFactory.buildStylist("Lebo", "M", "l@s.com", "071", "Cuts");
        appointment = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), 45, null);
    }

    @Test
    @DisplayName("Should create payment with PENDING status and zero discount")
    void shouldCreatePaymentWithDefaults() {
        Payment payment = PaymentFactory.buildPayment(
                appointment, new BigDecimal("150.00"), PaymentMethod.CASH);
        assertNotNull(payment);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(payment.getDiscount()));
        assertNotNull(payment.getPaymentId());
    }

    @Test
    @DisplayName("Should create payment with promotion and calculate final amount")
    void shouldCreatePaymentWithPromotion() {
        Promotion promo = PromotionFactory.buildPromotion(
                "SAVE50", "Save R50", DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"),
                LocalDate.now(), LocalDate.now().plusDays(10), 100);

        Payment payment = PaymentFactory.buildPaymentWithPromotion(
                appointment, new BigDecimal("200.00"),
                new BigDecimal("50.00"), PaymentMethod.CARD, promo);
        assertNotNull(payment);
        assertEquals(0, new BigDecimal("150.00").compareTo(payment.getFinalAmount()));
    }

    @Test
    @DisplayName("Should return null when appointment is null")
    void shouldReturnNullWhenAppointmentIsNull() {
        Payment payment = PaymentFactory.buildPayment(
                null, new BigDecimal("150"), PaymentMethod.CASH);
        assertNull(payment);
    }

    @Test
    @DisplayName("Should return null when amount is zero or negative")
    void shouldReturnNullWhenAmountIsInvalid() {
        Payment payment = PaymentFactory.buildPayment(
                appointment, BigDecimal.ZERO, PaymentMethod.CASH);
        assertNull(payment);
    }

    @Test
    @DisplayName("Should return null when payment method is null")
    void shouldReturnNullWhenMethodIsNull() {
        Payment payment = PaymentFactory.buildPayment(
                appointment, new BigDecimal("100"), null);
        assertNull(payment);
    }
}