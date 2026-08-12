package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.valueobject.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    private String promotionId;
    private String code;
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;

    @Enumerated
    private DateRange dateRange;

    private int usageLimit;
    private int usageCount;

    protected Promotion() {}

    public Promotion(Builder builder) {
        this.promotionId   = builder.promotionId;
        this.code          = builder.code;
        this.description   = builder.description;
        this.discountType  = builder.discountType;
        this.discountValue = builder.discountValue;
        this.dateRange = builder.dateRange;
        this.usageLimit    = builder.usageLimit;
        this.usageCount    = builder.usageCount;
    }

    public String getPromotionId()        { return promotionId; }
    public String getCode()               { return code; }
    public String getDescription()        { return description; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue()  { return discountValue; }
    public DateRange getDateRange()       {return dateRange;}
    public int getUsageLimit()            { return usageLimit; }
    public int getUsageCount()            { return usageCount; }

    public static class Builder {
        private String promotionId;
        private String code;
        private String description;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private DateRange dateRange;
        private int usageLimit;
        private int usageCount;


        public Builder setPromotionId(String promotionId) {
            this.promotionId = promotionId; return this;
        }
        public Builder setCode(String code) {
            this.code = code; return this;
        }
        public Builder setDescription(String description) {
            this.description = description; return this;
        }
        public Builder setDiscountType(DiscountType discountType) {
            this.discountType = discountType; return this;
        }
        public Builder setDiscountValue(BigDecimal discountValue) {
            this.discountValue = discountValue; return this;
        }

        public Builder setDateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder setUsageLimit(int usageLimit) {
            this.usageLimit = usageLimit; return this;
        }
        public Builder setUsageCount(int usageCount) {
            this.usageCount = usageCount; return this;
        }

        public Promotion build() {
            return new Promotion(this);
        }
    }
}
