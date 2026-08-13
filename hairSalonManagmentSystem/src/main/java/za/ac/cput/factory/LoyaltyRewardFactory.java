package za.ac.cput.factory;

import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.domain.enums.LoyaltyTier;
import za.ac.cput.util.Helper;

import java.time.LocalDateTime;

public class LoyaltyRewardFactory {

    public static LoyaltyReward buildLoyaltyReward(Customer customer){
        if (customer == null) return null;

        return new LoyaltyReward.Builder()
                .setLoyaltyId(Helper.generateId())
                .setCustomer(customer)
                .setPointsBalance(0)
                .setTotalEarned(0)
                .setTotalRedeemed(0)
                .setTier(LoyaltyTier.BRONZE)
                .setLastUpdated(LocalDateTime.now())
                .build();
    }
}
