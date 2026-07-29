package za.ac.cput.factory;

import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.domain.enums.LoyaltyTier;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoyaltyRewardFactory {

    public static LoyaltyReward buildLoyaltyReward(Customer customer){
        if (customer == null) return null;

        return new LoyaltyReward.Builder()
                .setLoyaltyId(UUID.randomUUID().toString())
                .setCustomer(customer)
                .setPointsBalance(0)
                .setTotalEarned(0)
                .setTotalRedeemed(0)
                .setTier(LoyaltyTier.BRONZE)
                .setLastUpdated(LocalDateTime.now())
                .build();
    }
}
