package za.ac.cput.factory;

import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PromotionFactory {

    public static Promotion buildPromotion(String code, String description,
                                           DiscountType discountType, BigDecimal discountValue,
                                           LocalDate startDate, LocalDate endDate,
                                           int usageLimit){
        if (code == null || code.isBlank()) return null;
        if (discountType == null) return null;
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) return null;
        if (discountType == DiscountType.PERCENTAGE
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) return null;
        if (startDate == null || endDate == null) return null;
        if (endDate.isBefore(startDate)) return null;

        return new Promotion.Builder()
                .setPromotionId(UUID.randomUUID().toString())
                .setCode(code.toUpperCase().trim())
                .setDescription(description)
                .setDiscountType(discountType)
                .setDiscountValue(discountValue)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setUsageLimit(usageLimit)
                .setUsageCount(0)
                .build();

    }
}
