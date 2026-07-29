package za.ac.cput.factory;

import za.ac.cput.domain.ProductInventory;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductInventoryFactory {

    public static ProductInventory buildProduct(String name, String brand,
                                                String category, int stockQuantity,
                                                int reorderLevel, BigDecimal costPrice,
                                                BigDecimal sellingPrice){
        if (name == null || name.isBlank()) return null;
        if (stockQuantity < 0 || reorderLevel < 0) return null;
        if (costPrice == null || sellingPrice == null) return null;
        if (sellingPrice.compareTo(BigDecimal.ZERO) <= 0) return null;

        return new ProductInventory.Builder()
                .setProductId(UUID.randomUUID().toString())
                .setName(name.trim())
                .setBrand(brand)
                .setCategory(category)
                .setStockQuantity(stockQuantity)
                .setReorderLevel(reorderLevel)
                .setCostPrice(costPrice)
                .setSellingPrice(sellingPrice)
                .build();
    }
}
