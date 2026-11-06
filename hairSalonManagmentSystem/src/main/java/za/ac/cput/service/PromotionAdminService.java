package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.valueobject.DateRange;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.PromotionRepository;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Admin-side promotion changes: create, edit, end early, reopen, delete.
 *
 * All validation lives here so it holds for every caller, not just the admin page's form.
 * Edits never write usageCount (see PromotionRepository.updateDetails), so editing a promotion
 * can't reset or roll back the number of times it has been used.
 */
@Service
public class PromotionAdminService {

    private static final Pattern CODE = Pattern.compile("[A-Z0-9_-]{3,20}");
    private static final int MAX_DESCRIPTION = 255;

    public record Details(String code, String description, DiscountType discountType,
                          BigDecimal discountValue, LocalDate startDate, LocalDate endDate,
                          Integer usageLimit) {}

    private final PromotionRepository promotions;
    private final PromotionOfferService offers;

    public PromotionAdminService(PromotionRepository promotions, PromotionOfferService offers) {
        this.promotions = promotions;
        this.offers = offers;
    }

    @Transactional
    public Promotion create(Details d) {
        String code = validate(d, null);
        if (d.endDate().isBefore(LocalDate.now()))
            throw new InvalidOperationException("The end date has already passed, so this promotion would never run.");
        return promotions.save(new Promotion.Builder()
                .setPromotionId(Helper.generateId())
                .setCode(code)
                .setDescription(blankToNull(d.description()))
                .setDiscountType(d.discountType())
                .setDiscountValue(d.discountValue())
                .setDateRange(DateRange.of(d.startDate(), d.endDate()))
                .setUsageLimit(d.usageLimit())
                .setUsageCount(0)
                .setEnded(false)
                .build());
    }

    @Transactional
    public Promotion update(String id, Details d) {
        Promotion existing = read(id);
        String code = validate(d, existing);
        int updated = promotions.updateDetails(id, code, blankToNull(d.description()), d.discountType(),
                d.discountValue(), d.startDate(), d.endDate(), d.usageLimit());
        if (updated == 0) {
            // The only way the guarded UPDATE matches nothing for an existing row.
            throw new InvalidOperationException("The usage limit can't be lower than the "
                    + read(id).getUsageCount() + " uses already counted.");
        }
        return read(id);
    }

    /**
     * Stops the promotion now, keeping its history. From this moment it can't be booked or
     * redeemed - including by bookings that already hold it, which pay full price unless staff
     * choose another code when recording the payment.
     */
    @Transactional
    public Promotion end(String id) {
        read(id);
        promotions.setEnded(id, true);
        return read(id);
    }

    @Transactional
    public Promotion reopen(String id) {
        Promotion p = read(id);
        if (p.getDateRange() == null || p.getEndDate().isBefore(LocalDate.now()))
            throw new InvalidOperationException("This promotion's end date has passed. Edit the dates to run it again.");
        promotions.setEnded(id, false);
        return read(id);
    }

    /**
     * A promotion with history (payments or bookings holding it) is ended, not deleted: deleting
     * it would orphan the codes on those bookings and the discount records on those payments.
     */
    @Transactional
    public void delete(String id) {
        Promotion p = read(id);
        boolean hasHistory = offers.usageStats().stream()
                .filter(s -> s.promotionId().equals(id))
                .anyMatch(s -> s.redeemed() + s.heldByBookings() + s.refunded() > 0);
        if (hasHistory || p.getUsageCount() > 0)
            throw new InvalidOperationException("Promotion " + p.getCode()
                    + " has already been used or booked, so it can't be deleted. End it instead.");
        promotions.deleteById(id);
    }

    private Promotion read(String id) {
        return promotions.findById(id).orElseThrow(() -> ResourceNotFoundException.forEntity("Promotion", id));
    }

    /** Returns the normalised code. Throws with a sentence the admin page can show as-is. */
    private String validate(Details d, Promotion existing) {
        if (d == null) throw new InvalidOperationException("Promotion details are required.");
        String code = d.code() == null ? "" : d.code().trim().toUpperCase(Locale.ROOT);
        if (!CODE.matcher(code).matches())
            throw new InvalidOperationException("The code must be 3 to 20 letters, numbers, - or _ (no spaces).");
        promotions.findByCodeIgnoreCase(code)
                .filter(other -> existing == null || !other.getPromotionId().equals(existing.getPromotionId()))
                .ifPresent(other -> { throw new InvalidOperationException("Another promotion already uses the code " + code + "."); });
        if (d.description() != null && d.description().length() > MAX_DESCRIPTION)
            throw new InvalidOperationException("The description can be at most " + MAX_DESCRIPTION + " characters.");
        if (d.discountType() == null)
            throw new InvalidOperationException("Choose a discount type.");
        if (d.discountValue() == null || d.discountValue().signum() <= 0)
            throw new InvalidOperationException("The discount must be more than zero.");
        if (d.discountValue().stripTrailingZeros().scale() > 2)
            throw new InvalidOperationException("The discount can have at most two decimal places.");
        if (d.discountType() == DiscountType.PERCENTAGE && d.discountValue().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new InvalidOperationException("A percentage discount can't be more than 100%.");
        if (d.startDate() == null || d.endDate() == null)
            throw new InvalidOperationException("Choose a start date and an end date.");
        if (d.endDate().isBefore(d.startDate()))
            throw new InvalidOperationException("The end date can't be before the start date.");
        if (d.usageLimit() == null || d.usageLimit() < 1)
            throw new InvalidOperationException("The usage limit must be at least 1.");
        if (existing != null && d.usageLimit() < existing.getUsageCount())
            throw new InvalidOperationException("The usage limit can't be lower than the "
                    + existing.getUsageCount() + " uses already counted.");
        return code;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
