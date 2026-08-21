package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.LoyaltyRewardFactory;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.repository.LoyaltyRewardRepository;
import za.ac.cput.service.ILoyaltyRewardService;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoyaltyRewardServiceImpl implements ILoyaltyRewardService {

    private final LoyaltyRewardRepository repository;
    private final CustomerRepository customerRepository;

    public LoyaltyRewardServiceImpl(LoyaltyRewardRepository repository,
                                    CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    public LoyaltyReward create(LoyaltyReward loyaltyReward){
        return repository.save(loyaltyReward);
    }

    public LoyaltyReward read(String id){
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("LoyaltyReward", id));
    }

    public LoyaltyReward update(LoyaltyReward loyaltyReward){
        read(loyaltyReward.getLoyaltyId());
        return repository.save(loyaltyReward);
    }

    public void delete(String id){
        repository.deleteById(id);
    }

    public List<LoyaltyReward> getAll() {
        return repository.findAll();
    }

    @Override
    public LoyaltyReward getOrCreate(String customerId) {
        return repository.findByCustomer_CustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", customerId));
                    LoyaltyReward reward = LoyaltyRewardFactory.buildLoyaltyReward(customer);
                    if (reward == null) {
                        throw new InvalidOperationException("Could not create a loyalty reward for this customer.");
                    }
                    return create(reward);
                });
    }

    @Override
    public LoyaltyReward addPoints(String customerId, BigDecimal amountSpent) {
        LoyaltyReward existing = getOrCreate(customerId);
        int pointsEarned = Helper.calculateLoyaltyPointsEarned(amountSpent);
        int totalEarned = existing.getTotalEarned() + pointsEarned;

        LoyaltyReward updated = new LoyaltyReward.Builder()
                .setLoyaltyId(existing.getLoyaltyId())
                .setCustomer(existing.getCustomer())
                .setPointsBalance(existing.getPointsBalance() + pointsEarned)
                .setTotalEarned(totalEarned)
                .setTotalRedeemed(existing.getTotalRedeemed())
                .setTier(Helper.determineLoyaltyTier(totalEarned))
                .setLastUpdated(LocalDateTime.now())
                .build();
        return repository.save(updated);
    }

    @Override
    public LoyaltyReward redeemPoints(String customerId, int points) {
        LoyaltyReward existing = getOrCreate(customerId);
        if (points <= 0 || points > existing.getPointsBalance()) {
            throw new InvalidOperationException("Cannot redeem " + points + " points; balance is "
                    + existing.getPointsBalance() + ".");
        }

        LoyaltyReward updated = new LoyaltyReward.Builder()
                .setLoyaltyId(existing.getLoyaltyId())
                .setCustomer(existing.getCustomer())
                .setPointsBalance(existing.getPointsBalance() - points)
                .setTotalEarned(existing.getTotalEarned())
                .setTotalRedeemed(existing.getTotalRedeemed() + points)
                .setTier(existing.getTier())
                .setLastUpdated(LocalDateTime.now())
                .build();
        return repository.save(updated);
    }

}
