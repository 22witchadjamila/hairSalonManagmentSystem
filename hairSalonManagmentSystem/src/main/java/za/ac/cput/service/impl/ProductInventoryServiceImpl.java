package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.ProductInventory;
import za.ac.cput.domain.SalonService;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.ProductInventoryFactory;
import za.ac.cput.repository.ProductInventoryRepository;
import za.ac.cput.service.IProductInventoryService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductInventoryServiceImpl implements IProductInventoryService {

    private final ProductInventoryRepository repository;

    public ProductInventoryServiceImpl(ProductInventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductInventory create(ProductInventory productInventory) {
        return repository.save(productInventory);
    }

    @Override
    public ProductInventory read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("ProductInventory", id));
    }

    @Override
    public ProductInventory update(ProductInventory productInventory) {
        read(productInventory.getProductId());
        return repository.save(productInventory);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<ProductInventory> getAll() {
        return repository.findAll();
    }

    @Override
    public ProductInventory register(String name, String brand, String category, int stockQuantity,
                                      int reorderLevel, BigDecimal costPrice, BigDecimal sellingPrice,
                                      SalonService salonService) {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                name, brand, category, stockQuantity, reorderLevel, costPrice, sellingPrice, salonService);
        if (product == null) {
            throw new InvalidOperationException("Invalid product details provided.");
        }
        return create(product);
    }

    @Override
    public ProductInventory adjustStock(String productId, int delta) {
        ProductInventory existing = read(productId);
        int newQuantity = existing.getStockQuantity() + delta;
        if (newQuantity < 0) {
            throw new InvalidOperationException("Stock adjustment would leave a negative quantity.");
        }

        ProductInventory updated = new ProductInventory.Builder()
                .setProductId(existing.getProductId())
                .setName(existing.getName())
                .setBrand(existing.getBrand())
                .setCategory(existing.getCategory())
                .setStockQuantity(newQuantity)
                .setReorderLevel(existing.getReorderLevel())
                .setCostPrice(existing.getCostPrice())
                .setSellingPrice(existing.getSellingPrice())
                .setSalonService(existing.getSalonService())
                .build();
        return repository.save(updated);
    }

    @Override
    public List<ProductInventory> lowStock() {
        return repository.findAll().stream()
                .filter(product -> product.getStockQuantity() <= product.getReorderLevel())
                .toList();
    }
}
