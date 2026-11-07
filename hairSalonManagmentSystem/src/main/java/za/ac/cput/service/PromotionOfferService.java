package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.repository.PromotionRepository;
import za.ac.cput.repository.SalonServiceRepository;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Promotions as customers experience them.
 *
 * Usage policy - what "times used" means, so the number can be trusted:
 *   1. Admin creates a code with a date range and a usage limit.
 *   2. A customer sees it (available) and gets a price preview (quote) - both calculated HERE.
 *   3. The customer books with the code. The booking HOLDS a use ("booked, not yet paid"): it
 *      is shown to the admin and counts against the limit when someone else tries to book, so
 *      a limited offer can't be over-promised. It is not a use yet - the visit may not happen.
 *   4. Staff record the in-store payment. The code is re-validated, the discount is calculated
 *      on the server, and ONE use is counted atomically (redeem). A second payment for the same
 *      appointment is refused, so a retry or a double-click can't count twice.
 *   5. A refund gives the use back (unredeem). Cancelled and no-show bookings release their
 *      hold. Failed payments never count. Paid payments can't be deleted, only refunded, so the
 *      count can't drift by removing rows.
 * So "times used" = PAID payments that used the code, and usageCount is kept equal to that
 * (PromotionUsageReconciler re-derives it at startup in case older code let it drift).
 *
 * A promotion is ACTIVE when today falls inside its date range, it has uses left, and an admin
 * hasn't ended it early.
 */
@Service
public class PromotionOfferService {

    public record Offer(String code, String description, DiscountType discountType,
                        BigDecimal discountValue, LocalDate validUntil) {}

    public record Quote(boolean valid, String message, String code,
                        BigDecimal price, BigDecimal discount, BigDecimal finalPrice) {}

    private final PromotionRepository promotions;
    private final SalonServiceRepository services;
    private final AppointmentRepository appointments;
    private final PaymentRepository payments;

    public PromotionOfferService(PromotionRepository promotions, SalonServiceRepository services,
                                 AppointmentRepository appointments, PaymentRepository payments) {
        this.promotions = promotions;
        this.services = services;
        this.appointments = appointments;
        this.payments = payments;
    }

    public static boolean isActive(Promotion p, LocalDate today) {
        return !p.isEnded() && p.getDateRange() != null && p.getDateRange().contains(today)
                && p.getUsageCount() < p.getUsageLimit();
    }

    /** One status, computed in one place, so the admin table and the booking page agree. */
    public static String statusOf(Promotion p, LocalDate today) {
        if (p.isEnded()) return "ENDED";
        if (p.getDateRange() == null) return "EXPIRED";
        if (today.isBefore(p.getStartDate())) return "SCHEDULED";
        if (today.isAfter(p.getEndDate())) return "EXPIRED";
        if (p.getUsageCount() >= p.getUsageLimit()) return "FULLY_USED";
        return "ACTIVE";
    }

    /**
     * Admin figures for every promotion, read from the payment and appointment rows themselves
     * rather than from a stored counter.
     */
    public record UsageStats(String promotionId, String code, long redeemed, long heldByBookings,
                             long refunded, int usageLimit, long remaining, String status) {}

    private static final List<AppointmentStatus> HOLDING =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED);
    private static final List<PaymentStatus> SETTLED = List.of(PaymentStatus.PAID, PaymentStatus.REFUNDED);

    public List<UsageStats> usageStats() {
        Map<String, Long> paid = toMap(payments.countByPromotionAndStatus(PaymentStatus.PAID));
        Map<String, Long> refunded = toMap(payments.countByPromotionAndStatus(PaymentStatus.REFUNDED));
        Map<String, Long> held = toMap(appointments.countUnpaidBookingsByPromoCode(HOLDING, SETTLED));
        LocalDate today = LocalDate.now();
        return promotions.findAll().stream().map(p -> {
            long redeemed = paid.getOrDefault(p.getPromotionId(), 0L);
            long holding = held.getOrDefault(p.getCode().toUpperCase(Locale.ROOT), 0L);
            return new UsageStats(p.getPromotionId(), p.getCode(), redeemed, holding,
                    refunded.getOrDefault(p.getPromotionId(), 0L), p.getUsageLimit(),
                    Math.max(0, p.getUsageLimit() - redeemed - holding), statusOf(p, today));
        }).toList();
    }

    /** Uses still free for a NEW booking: the limit minus redeemed uses and unpaid holds. */
    long freeForBooking(Promotion p) {
        long held = appointments.countUnpaidBookingsByPromoCode(HOLDING, SETTLED).stream()
                .filter(row -> p.getCode().equalsIgnoreCase((String) row[0]))
                .mapToLong(row -> (Long) row[1]).sum();
        return p.getUsageLimit() - p.getUsageCount() - held;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> out = new HashMap<>();
        for (Object[] row : rows) out.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        return out;
    }

    /** Public list: only promotions a customer could actually use today. */
    public List<Offer> available() {
        LocalDate today = LocalDate.now();
        return promotions.findAll().stream()
                .filter(p -> isActive(p, today))
                .map(p -> new Offer(p.getCode(), p.getDescription(), p.getDiscountType(),
                        p.getDiscountValue(), p.getDateRange().getEndDate()))
                .toList();
    }

    /**
     * Price preview for one service with one code. Never throws for a bad code - the booking page
     * shows the message - so a typo is an answer, not an error.
     */
    public Quote quote(String code, String serviceId) {
        SalonService service = services.findById(serviceId)
                .filter(SalonService::isActive)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Service", serviceId));
        BigDecimal price = service.getPrice().getValue();
        try {
            Promotion promo = requireUsable(code);
            BigDecimal discount = discountOn(price, promo);
            return new Quote(true, describe(promo), promo.getCode(), price, discount,
                    Helper.calculateFinalAmount(price, discount));
        } catch (InvalidOperationException | ResourceNotFoundException e) {
            return new Quote(false, e.getMessage(), null, price, BigDecimal.ZERO, price);
        }
    }

    /** For booking and for payment: the promotion, or a clear reason it can't be used. */
    public Promotion requireUsable(String code) {
        if (code == null || code.isBlank())
            throw new InvalidOperationException("Enter a promotion code.");
        Promotion promo = promotions.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("There's no promotion with the code " + code.trim().toUpperCase() + "."));
        LocalDate today = LocalDate.now();
        if (promo.isEnded())
            throw new InvalidOperationException("Promotion " + promo.getCode() + " has ended.");
        if (promo.getDateRange() == null || !promo.getDateRange().contains(today))
            throw new InvalidOperationException("Promotion " + promo.getCode() + " isn't running today.");
        if (promo.getUsageCount() >= promo.getUsageLimit())
            throw new InvalidOperationException("Promotion " + promo.getCode() + " has been fully used.");
        return promo;
    }

    /**
     * For booking: usable today AND not already promised to other unpaid bookings. Payment
     * uses requireUsable instead - the appointment being paid is itself one of the holds.
     */
    public Promotion requireBookable(String code) {
        Promotion promo = requireUsable(code);
        if (freeForBooking(promo) <= 0)
            throw new InvalidOperationException("Promotion " + promo.getCode()
                    + " has been fully booked. It can't be added to new appointments.");
        return promo;
    }

    public BigDecimal discountOn(BigDecimal price, Promotion promo) {
        return Helper.calculateDiscount(price, promo.getDiscountType(), promo.getDiscountValue());
    }

    /** Store the customer's chosen code on their appointment (intent only - see step 3). */
    @Transactional
    public Appointment attachToAppointment(Appointment appointment, Promotion promo) {
        return appointments.save(new Appointment.Builder()
                .setAppointmentId(appointment.getAppointmentId())
                .setCustomer(appointment.getCustomer())
                .setStylist(appointment.getStylist())
                .setSalonService(appointment.getSalonService())
                .setAppointmentDate(appointment.getAppointmentDate())
                .setTimeSlot(appointment.getTimeSlot())
                .setNotes(appointment.getNotes())
                .setPromoCode(promo.getCode())
                .setCreatedAt(appointment.getCreatedAt())
                .setStatus(appointment.getStatus())
                .build());
    }

    /** Count one use. Atomic; fails cleanly if another payment took the last use first. */
    @Transactional
    public void redeem(Promotion promo) {
        if (promotions.incrementUsageIfAvailable(promo.getPromotionId()) == 0)
            throw new InvalidOperationException("Promotion " + promo.getCode() + " has been fully used.");
    }

    @Transactional
    public void unredeem(Promotion promo) {
        promotions.decrementUsage(promo.getPromotionId());
    }

    private static String describe(Promotion p) {
        String amount = p.getDiscountType() == DiscountType.PERCENTAGE
                ? p.getDiscountValue().stripTrailingZeros().toPlainString() + "% off"
                : "R" + p.getDiscountValue().setScale(2, java.math.RoundingMode.HALF_UP) + " off";
        return p.getCode() + " applied: " + amount + ".";
    }
}
