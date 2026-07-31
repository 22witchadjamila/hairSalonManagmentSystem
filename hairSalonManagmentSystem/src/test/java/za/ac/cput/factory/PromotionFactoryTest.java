package za.ac.cput.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PromotionFactory Tests")
class PromotionFactoryTest {

    @Test
    @DisplayName("Should create a valid promotion with uppercased code")
    void shouldCreateValidPromotion() {
        Promotion promo = PromotionFactory.buildPromotion(
                "summer20", "Summer special",
                DiscountType.PERCENTAGE, new BigDecimal("20"),
                LocalDate.now(), LocalDate.now().plusDays(30), 100);
        assertNotNull(promo);
        assertEquals("SUMMER20", promo.getCode());
        assertEquals(0, promo.getUsageCount());
        assertNotNull(promo.getPromotionId());
    }

    @Test
    @DisplayName("Should return null when code is blank")
    void shouldReturnNullWhenCodeIsBlank() {
        Promotion promo = PromotionFactory.buildPromotion(
                "", "desc", DiscountType.FIXED_AMOUNT, new BigDecimal("50"),
                LocalDate.now(), LocalDate.now().plusDays(10), 10);
        assertNull(promo);
    }

    @Test
    @DisplayName("Should return null when percentage exceeds 100")
    void shouldReturnNullWhenPercentageOver100() {
        Promotion promo = PromotionFactory.buildPromotion(
                "OVER", "desc", DiscountType.PERCENTAGE, new BigDecimal("110"),
                LocalDate.now(), LocalDate.now().plusDays(10), 10);
        assertNull(promo);
    }

    @Test
    @DisplayName("Should return null when end date is before start date")
    void shouldReturnNullWhenEndBeforeStart() {
        Promotion promo = PromotionFactory.buildPromotion(
                "PROMO", "desc", DiscountType.FIXED_AMOUNT, new BigDecimal("50"),
                LocalDate.now().plusDays(5), LocalDate.now(), 10);
        assertNull(promo);
    }

    @Test
    @DisplayName("Should return null when discount value is zero")
    void shouldReturnNullWhenDiscountIsZero() {
        Promotion promo = PromotionFactory.buildPromotion(
                "ZERO", "desc", DiscountType.FIXED_AMOUNT, BigDecimal.ZERO,
                LocalDate.now(), LocalDate.now().plusDays(10), 10);
        assertNull(promo);
    }

    @Test
    @DisplayName("Should allow fixed amount discount above 100")
    void shouldAllowFixedAmountAbove100() {
        Promotion promo = PromotionFactory.buildPromotion(
                "BIG", "desc", DiscountType.FIXED_AMOUNT, new BigDecimal("150"),
                LocalDate.now(), LocalDate.now().plusDays(10), 10);
        assertNotNull(promo);
    }
}
