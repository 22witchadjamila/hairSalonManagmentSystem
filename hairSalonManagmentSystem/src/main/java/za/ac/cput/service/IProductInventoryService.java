package za.ac.cput.service;

import za.ac.cput.domain.ProductInventory;
import za.ac.cput.domain.SalonService;

import java.math.BigDecimal;
import java.util.List;

public interface IProductInventoryService extends IService<ProductInventory, String>{
    ProductInventory register(String name, String brand, String category, int stockQuantity,
                              int reorderLevel, BigDecimal costPrice,
                              BigDecimal sellingPrice, SalonService salonService);

    /** Adjusts stock by delta (positive to restock to consume); rejects if it
     * would go negative. */
    ProductInventory adjustStock(String productId, int delta);

    /** Products at or below their reorder level. */
    List<ProductInventory> lowStock();
}
