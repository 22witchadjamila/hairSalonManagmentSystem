package za.ac.cput.service;

import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IPromotionService extends IService<Promotion, String>{
    Promotion register(String code, String description, DiscountType discountType,
                       BigDecimal discountValue, LocalDate startDate, LocalDate endDate, int usageLimit);

    /** Looks up a promo code and checks it's within
     * its active window and usage limit*/
    Promotion validate(String code);

    /** Marks one redemption of the promotion (increments usageCount). */
    Promotion recordUsage(Promotion promotion);

}
