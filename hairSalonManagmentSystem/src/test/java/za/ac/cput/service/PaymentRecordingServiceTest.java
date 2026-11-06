package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.domain.enums.LoyaltyTier;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.valueobject.DateRange;
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRecordingService - in-store payment lifecycle")
class PaymentRecordingServiceTest {

    @Mock PaymentRepository payments;
    @Mock AppointmentRepository appointments;
    @Mock PromotionOfferService offers;
    @Mock ILoyaltyRewardService loyalty;
    @InjectMocks PaymentRecordingService service;

    private static LoyaltyReward account(int balance) {
        return new LoyaltyReward.Builder().setLoyaltyId("l1").setPointsBalance(balance).setTotalEarned(balance)
                .setTier(LoyaltyTier.BRONZE).build();
    }

    private Appointment appointment(AppointmentStatus status, String promoCode) {
        SalonService cut = new SalonService.Builder().setServiceId("svc1").setName("Cut")
                .setPrice(Money.of(new BigDecimal("200.00"))).setDurationMinutes(30).setActive(true).build();
        Customer jane = new Customer.Builder().setCustomerId("c1").setFirstName("Jane").setLastName("Doe").build();
        return new Appointment.Builder().setAppointmentId("a1").setSalonService(cut).setCustomer(jane)
                .setStatus(status).setPromoCode(promoCode).build();
    }

    @Test
    @DisplayName("prices from the booked service, marks PAID with a timestamp")
    void recordsPaidPaymentAtServicePrice() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment p = service.recordInStorePayment("a1", PaymentMethod.CASH, null);

        assertEquals(0, new BigDecimal("200.00").compareTo(p.getFinalAmount().getValue()));
        assertEquals(PaymentStatus.PAID, p.getStatus());
        assertNotNull(p.getPaidAt(), "a PAID payment must record when it was paid");
        verify(offers, never()).redeem(any());
    }

    @Test
    @DisplayName("applies the code the customer booked with, discount calculated server-side, one use counted")
    void appliesBookedPromotion() {
        Promotion tenOff = new Promotion.Builder().setPromotionId("p1").setCode("TEN").setDiscountType(DiscountType.PERCENTAGE)
                .setDiscountValue(new BigDecimal("10")).setDateRange(DateRange.of(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)))
                .setUsageLimit(5).setUsageCount(0).build();
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.CONFIRMED, "TEN")));
        when(offers.requireUsable("TEN")).thenReturn(tenOff);
        when(offers.discountOn(new BigDecimal("200.00"), tenOff)).thenReturn(new BigDecimal("20.00"));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment p = service.recordInStorePayment("a1", PaymentMethod.CARD, null);

        assertEquals(0, new BigDecimal("180.00").compareTo(p.getFinalAmount().getValue()));
        verify(offers).redeem(tenOff);
    }

    @Test
    @DisplayName("refuses to pay the same appointment twice")
    void refusesDuplicatePayment() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(payments.existsByAppointment_AppointmentIdAndStatus("a1", PaymentStatus.PAID)).thenReturn(true);

        assertThrows(InvalidOperationException.class, () -> service.recordInStorePayment("a1", PaymentMethod.CASH, null));
        verify(payments, never()).save(any());
    }

    @Test
    @DisplayName("refuses to take payment for a cancelled appointment")
    void refusesCancelledAppointment() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.CANCELLED, null)));

        assertThrows(InvalidOperationException.class, () -> service.recordInStorePayment("a1", PaymentMethod.CASH, null));
        verify(payments, never()).save(any());
    }

    @Test
    @DisplayName("refund flips PAID to REFUNDED and gives the promotion use back")
    void refundReturnsPromotionUse() {
        Promotion promo = new Promotion.Builder().setPromotionId("p1").setCode("TEN").build();
        Payment paid = new Payment.Builder().setPaymentId("pay1").setStatus(PaymentStatus.PAID).setPromotion(promo)
                .setFinalAmount(Money.of(new BigDecimal("180.00"))).build();
        when(payments.findById("pay1")).thenReturn(Optional.of(paid));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(PaymentStatus.REFUNDED, service.refund("pay1").getStatus());
        verify(offers).unredeem(promo);
    }

    @Test
    @DisplayName("dashboard summary counts only PAID as revenue")
    void summaryCountsOnlyPaid() {
        when(payments.findAll()).thenReturn(List.of(
                pay(PaymentStatus.PAID, "200"), pay(PaymentStatus.PAID, "100"),
                pay(PaymentStatus.PENDING, "999"), pay(PaymentStatus.REFUNDED, "50"), pay(PaymentStatus.FAILED, "75")));

        PaymentRecordingService.Summary s = service.summary();

        assertEquals(0, new BigDecimal("300").compareTo(s.collected()));
        assertEquals(2, s.paidCount());
        assertEquals(1, s.pendingCount());
        assertEquals(1, s.refundedCount());
        assertEquals(1, s.failedCount());
    }

    private static Payment pay(PaymentStatus status, String amount) {
        return new Payment.Builder().setStatus(status)
                .setFinalAmount(Money.of(new BigDecimal(amount))).setDiscount(Money.of(BigDecimal.ZERO)).build();
    }

    // ------------------------------------------------------------------ loyalty

    @Test
    @DisplayName("loyalty: a paid bill earns 1 point per R10 of what was actually paid")
    void paymentEarnsPoints() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.recordInStorePayment("a1", PaymentMethod.CASH, null);

        verify(loyalty).addPoints(eq("c1"), argThat(a -> a.compareTo(new BigDecimal("200.00")) == 0));
        verify(loyalty, never()).redeemPoints(any(), anyInt());
    }

    @Test
    @DisplayName("loyalty: points are spent at R0.50 each, recorded on the payment, and earnings use the reduced bill")
    void paymentSpendsPoints() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(loyalty.getOrCreate("c1")).thenReturn(account(300));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment p = service.recordInStorePayment("a1", PaymentMethod.CASH, null, 100);   // 100 pts = R50 off R200

        assertEquals(0, new BigDecimal("150.00").compareTo(p.getFinalAmount().getValue()));
        assertEquals(0, new BigDecimal("50.00").compareTo(p.getDiscount().getValue()));
        assertEquals(100, p.getLoyaltyPointsRedeemed());
        verify(loyalty).redeemPoints("c1", 100);
        verify(loyalty).addPoints(eq("c1"), argThat(a -> a.compareTo(new BigDecimal("150.00")) == 0));
    }

    @Test
    @DisplayName("loyalty: refuses below the minimum, above the balance, or worth more than the bill - and saves nothing")
    void refusesBadRedemptions() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(loyalty.getOrCreate("c1")).thenReturn(account(500));

        for (int points : new int[]{-5, 10, 49, 501, 401}) {   // 401 pts = R200.50 > R200 due
            assertThrows(InvalidOperationException.class,
                    () -> service.recordInStorePayment("a1", PaymentMethod.CASH, null, points), "points " + points);
        }
        verify(payments, never()).save(any());
        verify(loyalty, never()).redeemPoints(any(), anyInt());
        verify(loyalty, never()).addPoints(any(), any());
    }

    @Test
    @DisplayName("loyalty: exactly the bill's worth of points is allowed, leaving nothing to pay and nothing to earn")
    void pointsCanCoverWholeBill() {
        when(appointments.findById("a1")).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED, null)));
        when(loyalty.getOrCreate("c1")).thenReturn(account(1000));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment p = service.recordInStorePayment("a1", PaymentMethod.CASH, null, 400);   // R200 off R200

        assertEquals(0, BigDecimal.ZERO.compareTo(p.getFinalAmount().getValue()));
        verify(loyalty, never()).addPoints(any(), any());
    }

    @Test
    @DisplayName("loyalty: a refund takes back what the payment earned and returns what it spent")
    void refundReversesLoyalty() {
        Customer jane = new Customer.Builder().setCustomerId("c1").build();
        Appointment appt = new Appointment.Builder().setAppointmentId("a1").setCustomer(jane).build();
        Payment paid = new Payment.Builder().setPaymentId("pay1").setStatus(PaymentStatus.PAID).setAppointment(appt)
                .setFinalAmount(Money.of(new BigDecimal("150.00"))).setLoyaltyPointsRedeemed(100).build();
        when(payments.findById("pay1")).thenReturn(Optional.of(paid));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment refunded = service.refund("pay1");

        assertEquals(100, refunded.getLoyaltyPointsRedeemed());   // still on the record
        verify(loyalty).removeEarnedPoints(eq("c1"), argThat(a -> a.compareTo(new BigDecimal("150.00")) == 0));
        verify(loyalty).returnRedeemedPoints("c1", 100);
    }
}
