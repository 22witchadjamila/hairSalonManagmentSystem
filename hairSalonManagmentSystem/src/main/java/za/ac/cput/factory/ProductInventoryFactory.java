package za.ac.cput.factory;

import za.ac.cput.domain.ProductInventory;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.util.Helper;
import java.math.BigDecimal;

public class ProductInventoryFactory {

    public static ProductInventory buildProduct(String name, String brand,
                                                String category, int stockQuantity,
                                                int reorderLevel, BigDecimal costPrice,
                                                BigDecimal sellingPrice){
        return buildProduct(name, brand, category, stockQuantity, reorderLevel,
                costPrice, sellingPrice, null);
    }

    public static ProductInventory buildProduct(String name, String brand,
                                                String category, int stockQuantity,
                                                int reorderLevel, BigDecimal costPrice,
                                                BigDecimal sellingPrice, SalonService salonService){
        if(Helper.isNullOrEmpty(name)) return null;
        if(stockQuantity <= 0 || reorderLevel < 0) return null;
        if(costPrice == null || sellingPrice == null) return null;
        if(!Helper.isValidAmount(sellingPrice)) return null;

        return new ProductInventory.Builder()
                .setProductId(Helper.generateId())
                .setName(name.trim())
                .setBrand(brand)
                .setCategory(category)
                .setStockQuantity(stockQuantity)
                .setReorderLevel(reorderLevel)
                .setCostPrice(Money.of(costPrice))
                .setSellingPrice(Money.of(sellingPrice))
                .setSalonService(salonService)
                .build();
    }
}
