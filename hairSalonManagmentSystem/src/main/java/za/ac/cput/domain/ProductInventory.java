package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.valueobject.Money;

@Entity
@Table(name = "product_inventory")
public class ProductInventory {

    @Id
    private String productId;
    private String name;
    private String brand;
    private String category;
    private int stockQuantity;
    private int reorderLevel;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cost_price"))
    private Money costPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "selling_price"))
    private Money sellingPrice;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private SalonService salonService;

    protected ProductInventory() {}

    public ProductInventory(Builder builder) {
        this.productId     = builder.productId;
        this.name          = builder.name;
        this.brand         = builder.brand;
        this.category      = builder.category;
        this.stockQuantity = builder.stockQuantity;
        this.reorderLevel  = builder.reorderLevel;
        this.costPrice     = builder.costPrice;
        this.sellingPrice  = builder.sellingPrice;
        this.salonService  = builder.salonService;
    }

    public String getProductId()        { return productId; }
    public String getName()             { return name; }
    public String getBrand()            { return brand; }
    public String getCategory()         { return category; }
    public int getStockQuantity()       { return stockQuantity; }
    public int getReorderLevel()        { return reorderLevel; }
    public Money getCostPrice()         { return costPrice; }
    public Money getSellingPrice()      { return sellingPrice; }
    public SalonService getSalonService() { return salonService; }

    public static class Builder {
        private String productId;
        private String name;
        private String brand;
        private String category;
        private int stockQuantity;
        private int reorderLevel;
        private Money costPrice;
        private Money sellingPrice;
        private SalonService salonService;

        public Builder setProductId(String productId) {
            this.productId = productId; return this;
        }

        public Builder setName(String name) {
            this.name = name; return this;
        }

        public Builder setBrand(String brand) {
            this.brand = brand; return this;
        }

        public Builder setCategory(String category) {
            this.category = category; return this;
        }

        public Builder setStockQuantity(int stockQuantity) {
            this.stockQuantity = stockQuantity; return this;
        }

        public Builder setReorderLevel(int reorderLevel) {
            this.reorderLevel = reorderLevel; return this;
        }

        public Builder setCostPrice(Money costPrice) {
            this.costPrice = costPrice; return this;
        }

        public Builder setSellingPrice(Money sellingPrice) {
            this.sellingPrice = sellingPrice; return this;
        }

        public Builder setSalonService(SalonService salonService) {
            this.salonService = salonService; return this;
        }

        public ProductInventory build() {
            return new ProductInventory(this);
        }
    }
}
