package za.ac.cput.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

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
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

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
    }

    public String getProductId()        { return productId; }
    public String getName()             { return name; }
    public String getBrand()            { return brand; }
    public String getCategory()         { return category; }
    public int getStockQuantity()       { return stockQuantity; }
    public int getReorderLevel()        { return reorderLevel; }
    public BigDecimal getCostPrice()    { return costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }

    public static class Builder {
        private String productId;
        private String name;
        private String brand;
        private String category;
        private int stockQuantity;
        private int reorderLevel;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;

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

        public Builder setCostPrice(BigDecimal costPrice) {
            this.costPrice = costPrice; return this;
        }

        public Builder setSellingPrice(BigDecimal sellingPrice) {
            this.sellingPrice = sellingPrice; return this;
        }

        public ProductInventory build() {
            return new ProductInventory(this);
        }
    }
}
