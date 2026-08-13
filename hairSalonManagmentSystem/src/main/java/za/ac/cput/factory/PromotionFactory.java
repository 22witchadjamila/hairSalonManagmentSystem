package za.ac.cput.factory;

import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.valueobject.DateRange;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionFactory {

    public static Promotion buildPromotion(String code, String description,
                                           DiscountType discountType, BigDecimal discountValue,
                                           LocalDate startDate, LocalDate endDate,
                                           int usageLimit){
        if (Helper.isNullOrEmpty(code)) return null;
        if (discountType == null) return null;
        if (!Helper.isValidAmount(discountValue)) return null;
        if (discountType == DiscountType.PERCENTAGE
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) return null;
        if (startDate == null || endDate == null) return null;
        if (endDate.isBefore(startDate)) return null;

        return new Promotion.Builder()
                .setPromotionId(Helper.generateId())
                .setCode(code.toUpperCase().trim())
                .setDescription(description)
                .setDiscountType(discountType)
                .setDiscountValue(discountValue)
                .setDateRange(DateRange.of(startDate, endDate))
                .setUsageLimit(usageLimit)
                .setUsageCount(0)
                .build();

    }
}
