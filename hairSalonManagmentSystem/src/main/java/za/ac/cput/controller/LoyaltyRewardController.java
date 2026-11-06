package za.ac.cput.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.service.ILoyaltyRewardService;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-rewards")
public class LoyaltyRewardController {

    private static final BigDecimal MAX_MANUAL_AWARD = new BigDecimal("100000");

    private final ILoyaltyRewardService service;

    public LoyaltyRewardController(ILoyaltyRewardService service) {
        this.service = service;
    }

    public record AddPointsRequest(BigDecimal amountSpent) {}

    /** What the pages need to explain the programme. Read from the same constants the server uses. */
    public record LoyaltyRules(BigDecimal randSpentPerPoint, BigDecimal randPerPoint, int minRedeemPoints,
                               int silverFrom, int goldFrom, int platinumFrom) {}

    /** Any signed-in user. */
    @GetMapping("/rules")
    public LoyaltyRules rules() {
        return new LoyaltyRules(Helper.POINTS_PER_RAND_SPENT, Helper.LOYALTY_RAND_PER_POINT,
                Helper.LOYALTY_MIN_REDEEM_POINTS, Helper.SILVER_THRESHOLD, Helper.GOLD_THRESHOLD,
                Helper.PLATINUM_THRESHOLD);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@perm.granted() or @access.isCustomer(#customerId)")
    public LoyaltyReward getOrCreate(@PathVariable String customerId) {
        return service.getOrCreate(customerId);
    }

    /**
     * Staff only - a customer must not be able to award themselves points. This is for corrections
     * (a cash sale taken outside the app, a goodwill gesture): payments recorded in the app award
     * their own points, so awarding the same visit here as well would count it twice.
     */
    @PostMapping("/customer/{customerId}/add-points")
    @PreAuthorize("@perm.granted()")
    public LoyaltyReward addPoints(@PathVariable String customerId, @RequestBody AddPointsRequest request) {
        BigDecimal amount = request.amountSpent();
        if (amount == null || amount.signum() <= 0)
            throw new InvalidOperationException("Enter the amount spent, in rand.");
        if (amount.compareTo(MAX_MANUAL_AWARD) > 0)
            throw new InvalidOperationException("That's more than R" + MAX_MANUAL_AWARD.toPlainString()
                    + " - check the amount.");
        if (Helper.calculateLoyaltyPointsEarned(amount) == 0)
            throw new InvalidOperationException("R" + amount.toPlainString() + " earns no points: it's 1 point for every R"
                    + Helper.POINTS_PER_RAND_SPENT.toPlainString() + ".");
        return service.addPoints(customerId, amount);
    }

    @GetMapping("/{id}")
    public LoyaltyReward read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<LoyaltyReward> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
