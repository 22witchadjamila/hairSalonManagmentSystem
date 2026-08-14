package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.LoyaltyReward;

import java.util.Optional;

public interface LoyaltyRewardRepository extends JpaRepository<LoyaltyReward, String> {
    Optional<LoyaltyReward> findByCustomer_CustomerId(String  customerId);
}
