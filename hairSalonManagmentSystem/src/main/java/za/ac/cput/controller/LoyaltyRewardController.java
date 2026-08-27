package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.service.ILoyaltyRewardService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-rewards")
public class LoyaltyRewardController {

    private final ILoyaltyRewardService service;

    public LoyaltyRewardController(ILoyaltyRewardService service) {
        this.service = service;
    }

    public record AddPointsRequest(BigDecimal amountSpent) {}
    public record RedeemPointsRequest(int points) {}

    @GetMapping("/customer/{customerId}")
    public LoyaltyReward getOrCreate(@PathVariable String customerId) {
        return service.getOrCreate(customerId);
    }

    @PostMapping("/customer/{customerId}/add-points")
    public LoyaltyReward addPoints(@PathVariable String customerId, @RequestBody AddPointsRequest request) {
        return service.addPoints(customerId, request.amountSpent());
    }

    @PostMapping("/customer/{customerId}/redeem")
    public LoyaltyReward redeemPoints(@PathVariable String customerId, @RequestBody RedeemPointsRequest request) {
        return service.redeemPoints(customerId, request.points());
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
