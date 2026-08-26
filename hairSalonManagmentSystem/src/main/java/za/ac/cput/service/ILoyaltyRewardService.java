package za.ac.cput.service;

import za.ac.cput.domain.LoyaltyReward;

import java.math.BigDecimal;

public interface ILoyaltyRewardService extends IService<LoyaltyReward, String> {
    /** Returns the customer's loyalty reward, creating a fresh BRONZE one if they don't have one yet. */
    LoyaltyReward getOrCreate(String customerId);

    /** Earns points for money spent and re-evaluates the customer's tier. */
    LoyaltyReward addPoints(String customerId, BigDecimal amountSpent);

    /** Redeems points from the balance (does not affect tier, which is based on lifetime earned points). */
    LoyaltyReward redeemPoints(String customerId, int points);
}
