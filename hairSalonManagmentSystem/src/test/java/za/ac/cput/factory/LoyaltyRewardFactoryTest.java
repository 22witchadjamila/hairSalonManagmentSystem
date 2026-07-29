package za.ac.cput.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.domain.enums.LoyaltyTier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoyaltyRewardFactory Tests")
class LoyaltyRewardFactoryTest {

    @Test
    @DisplayName("Should create loyalty reward with zero points and BRONZE tier")
    void shouldCreateRewardWithDefaults() {
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", "082");
        LoyaltyReward reward = LoyaltyRewardFactory.buildLoyaltyReward(customer);

        assertNotNull(reward);
        assertEquals(0, reward.getPointsBalance());
        assertEquals(0, reward.getTotalEarned());
        assertEquals(0, reward.getTotalRedeemed());
        assertEquals(LoyaltyTier.BRONZE, reward.getTier());
        assertNotNull(reward.getLoyaltyId());
        assertNotNull(reward.getLastUpdated());
    }

    @Test
    @DisplayName("Should link reward to the correct customer")
    void shouldLinkRewardToCustomer() {
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", "082");
        LoyaltyReward reward = LoyaltyRewardFactory.buildLoyaltyReward(customer);

        assertNotNull(reward);
        assertEquals(customer, reward.getCustomer());
    }

    @Test
    @DisplayName("Should return null when customer is null")
    void shouldReturnNullWhenCustomerIsNull() {
        LoyaltyReward reward = LoyaltyRewardFactory.buildLoyaltyReward(null);
        assertNull(reward);
    }

    @Test
    @DisplayName("Should generate unique IDs for each reward")
    void shouldGenerateUniqueIds() {
        Customer c1 = CustomerFactory.buildCustomer("A", "B", "a@b.com", "001");
        Customer c2 = CustomerFactory.buildCustomer("C", "D", "c@d.com", "002");
        LoyaltyReward r1 = LoyaltyRewardFactory.buildLoyaltyReward(c1);
        LoyaltyReward r2 = LoyaltyRewardFactory.buildLoyaltyReward(c2);
        assertNotNull(r1);
        assertNotNull(r2);
        assertNotEquals(r1.getLoyaltyId(), r2.getLoyaltyId());
    }

}
