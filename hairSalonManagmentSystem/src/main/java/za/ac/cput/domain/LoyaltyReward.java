package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.LoyaltyTier;

import java.time.LocalDateTime;

@Entity
public class LoyaltyReward {

    @Id
    private String loyaltyId;
    private int pointsBalance;
    private int totalEarned;
    private int totalRedeemed;

    @Enumerated(EnumType.STRING)
    private LoyaltyTier tier;

    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    protected LoyaltyReward() {}

    public LoyaltyReward(Builder builder){
        this.loyaltyId = builder.loyaltyId;
        this.pointsBalance = builder.pointsBalance;
        this.totalEarned = builder.totalEarned;
        this.totalRedeemed = builder.totalRedeemed;
        this.tier = builder.tier;
        this.lastUpdated = builder.lastUpdated;
        this.customer = builder.customer;
    }

    public String getLoyaltyId() {return loyaltyId;}
    public int getPointsBalance() {return pointsBalance;}
    public int getTotalEarned() {return totalEarned;}
    public int getTotalRedeemed() {return totalRedeemed;}
    public LoyaltyTier getTier() {return tier;}
    public LocalDateTime getLastUpdated() {return lastUpdated;}
    public Customer getCustomer() {return customer;}

    public static class Builder{
        private String loyaltyId;
        private int pointsBalance;
        private int totalEarned;
        private int totalRedeemed;
        private LoyaltyTier tier;
        private LocalDateTime lastUpdated;
        private Customer customer;

        public Builder setLoyaltyId(String loyaltyId) {
            this.loyaltyId = loyaltyId;
            return this;
        }

        public Builder setPointsBalance(int pointsBalance) {
            this.pointsBalance = pointsBalance;
            return this;
        }

        public Builder setTotalEarned(int totalEarned) {
            this.totalEarned = totalEarned;
            return this;
        }

        public Builder setTotalRedeemed(int totalRedeemed) {
            this.totalRedeemed = totalRedeemed;
            return this;
        }

        public Builder setTier(LoyaltyTier tier) {
            this.tier = tier;
            return this;
        }

        public Builder setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public LoyaltyReward build(){
            return new LoyaltyReward(this);
        }
    }
}
