package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The payment lifecycle for a pay-in-store salon.
 *
 * There is no card gateway and no webhook: customers pay at the salon. The reliable confirmation
 * that money was received is a staff member recording it, so a recorded payment is PAID at that
 * moment, with paidAt set. Previously every payment was created PENDING and nothing could ever
 * move it to PAID - and nothing in the frontend created payments at all - which is why the
 * dashboard never reflected anything.
 *
 * Rules enforced here, not in the browser:
 *   - The amount is the service's price from the database. The client never sends a price.
 *   - The discount is calculated here from a promotion that is re-validated now.
 *   - One PAID payment per appointment; a second attempt is refused.
 *   - Cancelled and no-show appointments cannot be paid.
 *   - The promotion's use is counted in the same transaction, after the payment is saved, and
 *     atomically - if either step fails, neither happens.
 *   - A refund flips PAID -> REFUNDED and gives the promotion use back.
 *
 * PENDING and FAILED remain valid states (the enum and older data use them) and are reported
 * separately - they are never counted as revenue.
 */
@Service
public class PaymentRecordingService {

    public record Summary(BigDecimal collected, BigDecimal discountsGiven, long paidCount,
                          BigDecimal refunded, long refundedCount, long pendingCount, long failedCount) {}

    private final PaymentRepository payments;
    private final AppointmentRepository appointments;
    private final PromotionOfferService offers;
    private final ILoyaltyRewardService loyalty;

    public PaymentRecordingService(PaymentRepository payments, AppointmentRepository appointments,
                                   PromotionOfferService offers, ILoyaltyRewardService loyalty) {
        this.payments = payments;
        this.appointments = appointments;
        this.offers = offers;
        this.loyalty = loyalty;
    }

    /**
     * @param promoCodeOverride null = use the code the customer booked with (if any);
     *                          ""   = record without a promotion;
     *                          code = use this code instead.
     */
    @Transactional
    public Payment recordInStorePayment(String appointmentId, PaymentMethod method, String promoCodeOverride) {
        return recordInStorePayment(appointmentId, method, promoCodeOverride, null);
    }

    /**
     * Loyalty, all inside this one transaction so a payment never half-happens:
     *   - redeemPoints (optional): points the customer spends now, R0.50 off each, taken from their
     *     balance and remembered on the payment so a refund can give them back. Checked against the
     *     balance, the minimum, and the amount actually due (points can't make the bill negative).
     *   - the customer then earns 1 point per R10 of what they actually PAID (after promo and points).
     * A refund reverses both.
     */
    @Transactional
    public Payment recordInStorePayment(String appointmentId, PaymentMethod method, String promoCodeOverride,
                                        Integer redeemPoints) {
        if (method == null) throw new InvalidOperationException("Choose how the customer paid.");
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Appointment", appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW)
            throw new InvalidOperationException("A " + appointment.getStatus().name().toLowerCase().replace('_', '-')
                    + " appointment can't be paid.");
        if (payments.existsByAppointment_AppointmentIdAndStatus(appointmentId, PaymentStatus.PAID))
            throw new InvalidOperationException("This appointment has already been paid.");

        BigDecimal price = appointment.getSalonService().getPrice().getValue();

        String code = promoCodeOverride != null ? promoCodeOverride : appointment.getPromoCode();
        Promotion promo = (code == null || code.isBlank()) ? null : offers.requireUsable(code);
        BigDecimal discount = promo == null ? BigDecimal.ZERO : offers.discountOn(price, promo);

        String customerId = appointment.getCustomer().getCustomerId();
        int points = redeemPoints == null ? 0 : redeemPoints;
        BigDecimal pointsValue = BigDecimal.ZERO;
        if (points != 0) {
            pointsValue = checkPointsRedemption(customerId, points, Helper.calculateFinalAmount(price, discount));
        }
        BigDecimal totalDiscount = discount.add(pointsValue);
        BigDecimal finalAmount = Helper.calculateFinalAmount(price, totalDiscount);

        Payment saved = payments.save(new Payment.Builder()
                .setPaymentId(Helper.generateId())
                .setAppointment(appointment)
                .setAmount(Money.of(price))
                .setDiscount(Money.of(totalDiscount))
                .setFinalAmount(Money.of(finalAmount))
                .setLoyaltyPointsRedeemed(points)
                .setMethod(method)
                .setStatus(PaymentStatus.PAID)
                .setPaidAt(LocalDateTime.now())
                .setTransactionRef(Helper.generateTransactionReference())
                .setPromotion(promo)
                .build());
        if (promo != null) offers.redeem(promo);   // throws -> whole transaction rolls back
        if (points > 0) loyalty.redeemPoints(customerId, points);
        if (Helper.calculateLoyaltyPointsEarned(finalAmount) > 0) loyalty.addPoints(customerId, finalAmount);
        return saved;
    }

    /** The rand value of the points, or a sentence saying why they can't be spent on this bill. */
    private BigDecimal checkPointsRedemption(String customerId, int points, BigDecimal due) {
        if (points < 0) throw new InvalidOperationException("Points to use can't be negative.");
        if (points < Helper.LOYALTY_MIN_REDEEM_POINTS)
            throw new InvalidOperationException("The minimum is " + Helper.LOYALTY_MIN_REDEEM_POINTS
                    + " points (R" + Helper.loyaltyValueOf(Helper.LOYALTY_MIN_REDEEM_POINTS) + ").");
        int balance = loyalty.getOrCreate(customerId).getPointsBalance();
        if (points > balance)
            throw new InvalidOperationException("The customer has " + balance + " points, not " + points + ".");
        BigDecimal value = Helper.loyaltyValueOf(points);
        if (value.compareTo(due) > 0) {
            int most = Helper.maxLoyaltyPointsFor(due);
            throw new InvalidOperationException(points + " points are worth R" + value + ", more than the R"
                    + due.setScale(2, java.math.RoundingMode.HALF_UP) + " due. "
                    + (most >= Helper.LOYALTY_MIN_REDEEM_POINTS ? "Use at most " + most + " points."
                                                                : "This bill is too small to use points on."));
        }
        return value;
    }

    @Transactional
    public Payment refund(String paymentId) {
        Payment p = payments.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Payment", paymentId));
        if (p.getStatus() != PaymentStatus.PAID)
            throw new InvalidOperationException("Only a paid payment can be refunded.");

        Payment refunded = payments.save(new Payment.Builder()
                .setPaymentId(p.getPaymentId())
                .setAppointment(p.getAppointment())
                .setAmount(p.getAmount())
                .setDiscount(p.getDiscount())
                .setFinalAmount(p.getFinalAmount())
                .setMethod(p.getMethod())
                .setStatus(PaymentStatus.REFUNDED)
                .setPaidAt(p.getPaidAt())
                .setTransactionRef(p.getTransactionRef())
                .setPromotion(p.getPromotion())
                .setLoyaltyPointsRedeemed(p.getLoyaltyPointsRedeemed())
                .build());
        if (p.getPromotion() != null) offers.unredeem(p.getPromotion());
        if (p.getAppointment() != null && p.getAppointment().getCustomer() != null) {
            String customerId = p.getAppointment().getCustomer().getCustomerId();
            // Take back what the payment earned, give back what it spent.
            if (p.getFinalAmount() != null) loyalty.removeEarnedPoints(customerId, p.getFinalAmount().getValue());
            loyalty.returnRedeemedPoints(customerId, p.getLoyaltyPointsRedeemed());
        }
        return refunded;
    }

    /**
     * Only unpaid rows (pending, failed, refunded) can be removed. Deleting a PAID payment would
     * erase revenue and leave its promotion use counted with nothing behind it - refund instead.
     */
    @Transactional
    public void delete(String paymentId) {
        Payment p = payments.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Payment", paymentId));
        if (p.getStatus() == PaymentStatus.PAID)
            throw new InvalidOperationException("A paid payment can't be deleted. Refund it instead.");
        payments.delete(p);
    }

    /** Latest payment for an appointment - there may be a refunded one and a newer one. */
    public Optional<Payment> latestFor(String appointmentId) {
        return payments.findByAppointment_AppointmentIdOrderByPaidAtDesc(appointmentId).stream().findFirst();
    }

    /** Dashboard figures, computed from the actual payment rows. Only PAID counts as revenue. */
    public Summary summary() {
        List<Payment> all = payments.findAll();
        return new Summary(
                sum(all, PaymentStatus.PAID, true),
                sum(all, PaymentStatus.PAID, false),
                count(all, PaymentStatus.PAID),
                sum(all, PaymentStatus.REFUNDED, true),
                count(all, PaymentStatus.REFUNDED),
                count(all, PaymentStatus.PENDING),
                count(all, PaymentStatus.FAILED));
    }

    private static BigDecimal sum(List<Payment> all, PaymentStatus status, boolean finalAmount) {
        return all.stream()
                .filter(p -> p.getStatus() == status)
                .map(p -> finalAmount ? p.getFinalAmount() : p.getDiscount())
                .map(m -> m == null ? BigDecimal.ZERO : m.getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long count(List<Payment> all, PaymentStatus status) {
        return all.stream().filter(p -> p.getStatus() == status).count();
    }
}
