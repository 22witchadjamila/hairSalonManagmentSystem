package za.ac.cput.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.LoyaltyReward;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.service.ILoyaltyRewardService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoyaltyRewardController Tests")
public class LoyaltyRewardControllerTest {

    @Mock
    private ILoyaltyRewardService service;

    @InjectMocks
    private LoyaltyRewardController controller;

    @Test
    @DisplayName("getOrCreate() delegates to service.getOrCreate()")
    void getOrCreate_delegatesToService(){
        LoyaltyReward reward = mock(LoyaltyReward.class);
        when(service.getOrCreate("c1")).thenReturn(reward);

        assertEquals(reward, controller.getOrCreate("c1"));
    }

    @Test
    @DisplayName("addPoints() delegates to service.addPoints()")
    void addPoints_delegatesToService(){
        LoyaltyReward reward = mock(LoyaltyReward.class);
        BigDecimal amount = new BigDecimal("150.00");
        when(service.addPoints("c1", amount)).thenReturn(reward);

        LoyaltyRewardController.AddPointsRequest request =
                new LoyaltyRewardController.AddPointsRequest(amount);

        assertEquals(reward, controller.addPoints("c1", request));
    }

    @Test
    @DisplayName("rules() reports the same numbers the server uses")
    void rules_reportsServerConstants() {
        LoyaltyRewardController.LoyaltyRules rules = controller.rules();
        assertEquals(0, new BigDecimal("10").compareTo(rules.randSpentPerPoint()));
        assertEquals(0, new BigDecimal("0.50").compareTo(rules.randPerPoint()));
        assertEquals(50, rules.minRedeemPoints());
        assertEquals(500, rules.silverFrom());
        assertEquals(2000, rules.goldFrom());
        assertEquals(5000, rules.platinumFrom());
    }

    @Test
    @DisplayName("addPoints() refuses a blank, zero, absurd or too-small amount")
    void addPoints_validatesAmount() {
        for (String bad : new String[]{null, "0", "-5", "9.99", "100000.01"}) {
            BigDecimal amount = bad == null ? null : new BigDecimal(bad);
            assertThrows(InvalidOperationException.class,
                    () -> controller.addPoints("c1", new LoyaltyRewardController.AddPointsRequest(amount)), String.valueOf(bad));
        }
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("read() delegates to service.read()")
    void read_delegatesToService() {
        LoyaltyReward reward = mock(LoyaltyReward.class);
        when(service.read("l1")).thenReturn(reward);

        assertEquals(reward, controller.read("l1"));
    }

    @Test
    @DisplayName("getAll() delegates to service.getAll()")
    void getAll_delegatesToService() {
        List<LoyaltyReward> rewards = List.of(mock(LoyaltyReward.class));
        when(service.getAll()).thenReturn(rewards);

        assertEquals(rewards, controller.getAll());
    }

    @Test
    @DisplayName("delete() delegates to service.delete()")
    void delete_delegatesToService() {
        controller.delete("l1");

        verify(service).delete("l1");
    }

}
