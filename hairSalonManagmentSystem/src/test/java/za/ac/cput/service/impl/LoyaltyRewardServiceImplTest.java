package za.ac.cput.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.domain.enums.LoyaltyTier;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.CustomerFactory;
import za.ac.cput.factory.LoyaltyRewardFactory;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.repository.LoyaltyRewardRepository;
import za.ac.cput.service.impl.LoyaltyRewardServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoyaltyRewardServiceImpl Tests")
class LoyaltyRewardServiceImplTest {

    @Mock
    private LoyaltyRewardRepository repository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoyaltyRewardServiceImpl service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = CustomerFactory.buildCustomer("Jane", "Doe", "jane@example.com", "0821234567");
    }

    @Test
    @DisplayName("getOrCreate() returns the existing reward when the customer already has one")
    void getOrCreate_returnsExisting_whenPresent() {
        LoyaltyReward existing = LoyaltyRewardFactory.buildLoyaltyReward(customer);
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));

        assertEquals(existing, service.getOrCreate(customer.getCustomerId()));
        verifyNoInteractions(customerRepository); // no need to look the customer up again
    }

    @Test
    @DisplayName("getOrCreate() creates a fresh BRONZE reward when the customer doesn't have one yet")
    void getOrCreate_createsFresh_whenAbsent() {
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.empty());
        when(customerRepository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyReward result = service.getOrCreate(customer.getCustomerId());

        assertEquals(LoyaltyTier.BRONZE, result.getTier());
        assertEquals(0, result.getPointsBalance());
    }

    @Test
    @DisplayName("getOrCreate() throws ResourceNotFoundException when the customer itself doesn't exist")
    void getOrCreate_throws_whenCustomerMissing() {
        when(repository.findByCustomer_CustomerId("missing")).thenReturn(Optional.empty());
        when(customerRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getOrCreate("missing"));
    }

    @Test
    @DisplayName("addPoints() adds points earned (1 per R10) to both balance and total earned")
    void addPoints_addsEarnedPoints() {
        LoyaltyReward existing = LoyaltyRewardFactory.buildLoyaltyReward(customer); // starts at 0 points
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<LoyaltyReward> captor = ArgumentCaptor.forClass(LoyaltyReward.class);

        LoyaltyReward result = service.addPoints(customer.getCustomerId(), new BigDecimal("100.00")); // 100 / 10 = 10 points

        verify(repository).save(captor.capture());
        assertEquals(10, captor.getValue().getPointsBalance());
        assertEquals(10, captor.getValue().getTotalEarned());
        assertEquals(10, result.getPointsBalance());
    }

    @Test
    @DisplayName("addPoints() upgrades the tier once total earned crosses the SILVER threshold")
    void addPoints_upgradesTier_whenThresholdCrossed() {
        LoyaltyReward existing = LoyaltyRewardFactory.buildLoyaltyReward(customer);
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // R10 per point; 6000 spent -> 600 points earned, above the 500-point SILVER threshold
        LoyaltyReward result = service.addPoints(customer.getCustomerId(), new BigDecimal("6000.00"));

        assertEquals(LoyaltyTier.SILVER, result.getTier());
    }

    @Test
    @DisplayName("redeemPoints() subtracts from the balance and adds to total redeemed")
    void redeemPoints_subtractsFromBalance() {
        LoyaltyReward existing = new LoyaltyReward.Builder()
                .setLoyaltyId(LoyaltyRewardFactory.buildLoyaltyReward(customer).getLoyaltyId())
                .setCustomer(customer)
                .setPointsBalance(100)
                .setTotalEarned(100)
                .setTotalRedeemed(0)
                .setTier(LoyaltyTier.BRONZE)
                .setLastUpdated(java.time.LocalDateTime.now())
                .build();
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyReward result = service.redeemPoints(customer.getCustomerId(), 40);

        assertEquals(60, result.getPointsBalance());
        assertEquals(40, result.getTotalRedeemed());
    }

    @Test
    @DisplayName("redeemPoints() throws InvalidOperationException when redeeming more points than the balance holds")
    void redeemPoints_throws_whenInsufficientBalance() {
        LoyaltyReward existing = LoyaltyRewardFactory.buildLoyaltyReward(customer); // 0 points available
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));

        assertThrows(InvalidOperationException.class, () -> service.redeemPoints(customer.getCustomerId(), 50));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("redeemPoints() throws InvalidOperationException for a non-positive amount")
    void redeemPoints_throws_whenPointsNotPositive() {
        LoyaltyReward existing = LoyaltyRewardFactory.buildLoyaltyReward(customer);
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(existing));

        assertThrows(InvalidOperationException.class, () -> service.redeemPoints(customer.getCustomerId(), 0));
    }

    @Test
    @DisplayName("removeEarnedPoints() takes a refunded payment's points off balance and lifetime total, and the tier follows")
    void removeEarnedPoints_reversesAndDowngradesTier() {
        LoyaltyReward gold = new LoyaltyReward.Builder().setLoyaltyId("l1").setCustomer(customer)
                .setPointsBalance(2050).setTotalEarned(2050).setTotalRedeemed(0).setTier(LoyaltyTier.GOLD).build();
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(gold));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyReward result = service.removeEarnedPoints(customer.getCustomerId(), new BigDecimal("1000.00")); // 100 points

        assertEquals(1950, result.getPointsBalance());
        assertEquals(1950, result.getTotalEarned());
        assertEquals(LoyaltyTier.SILVER, result.getTier());   // 1950 is below the 2000 GOLD line
    }

    @Test
    @DisplayName("removeEarnedPoints() never takes the balance below zero when the points were already spent")
    void removeEarnedPoints_floorsBalanceAtZero() {
        LoyaltyReward spent = new LoyaltyReward.Builder().setLoyaltyId("l1").setCustomer(customer)
                .setPointsBalance(10).setTotalEarned(100).setTotalRedeemed(90).setTier(LoyaltyTier.BRONZE).build();
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(spent));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyReward result = service.removeEarnedPoints(customer.getCustomerId(), new BigDecimal("500.00")); // 50 points

        assertEquals(0, result.getPointsBalance());
        assertEquals(50, result.getTotalEarned());
    }

    @Test
    @DisplayName("returnRedeemedPoints() gives points back and reduces total redeemed")
    void returnRedeemedPoints_givesBack() {
        LoyaltyReward r = new LoyaltyReward.Builder().setLoyaltyId("l1").setCustomer(customer)
                .setPointsBalance(20).setTotalEarned(100).setTotalRedeemed(80).setTier(LoyaltyTier.BRONZE).build();
        when(repository.findByCustomer_CustomerId(customer.getCustomerId())).thenReturn(Optional.of(r));
        when(repository.save(any(LoyaltyReward.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyReward result = service.returnRedeemedPoints(customer.getCustomerId(), 60);

        assertEquals(80, result.getPointsBalance());
        assertEquals(20, result.getTotalRedeemed());
        assertEquals(100, result.getTotalEarned());
    }
}
