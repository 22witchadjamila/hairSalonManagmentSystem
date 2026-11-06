package za.ac.cput.controller;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.repository.ProductInventoryRepository;
import za.ac.cput.domain.ProductInventory;
import za.ac.cput.service.IProductInventoryService;
import za.ac.cput.service.ISalonServiceService;
import za.ac.cput.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/products")
public class ProductInventoryController {

    private final IProductInventoryService service;
    private final ISalonServiceService salonServiceService;
    private final ProductInventoryRepository productRepository;

    public ProductInventoryController(IProductInventoryService service,
                                      ISalonServiceService salonServiceService,
                                      ProductInventoryRepository productRepository) {
        this.service = service;
        this.salonServiceService = salonServiceService;
        this.productRepository = productRepository;
    }

    /** serviceId matches the service_id column; the old name salonServiceId is still accepted. */
    public record ProductRequest(String name, String brand, String category, int stockQuantity,
                                 int reorderLevel, BigDecimal costPrice, BigDecimal sellingPrice,
                                 @JsonAlias("salonServiceId") String serviceId) {}

    public record ForSaleRequest(boolean forSale) {}

    /**
     * What customers may see about a retail product. Deliberately excludes cost price, reorder
     * level, the exact stock count and supplier/service links - those are internal.
     */
    public record ShopProduct(String productId, String name, String brand, String category,
                              BigDecimal price, String availability) {
        static ShopProduct of(ProductInventory p) {
            int stock = p.getStockQuantity();
            String availability = stock <= 0 ? "OUT_OF_STOCK"
                    : stock <= p.getReorderLevel() ? "LOW_STOCK" : "IN_STOCK";
            return new ShopProduct(p.getProductId(), p.getName(), p.getBrand(), p.getCategory(),
                    p.getSellingPrice() == null ? null : p.getSellingPrice().getValue(), availability);
        }
    }

    /** Public. Retail products only - the admin marks which ones with the for-sale toggle. */
    @GetMapping("/shop")
    public java.util.List<ShopProduct> shop() {
        return productRepository.findByForSaleTrue().stream().map(ShopProduct::of).toList();
    }

    /** Admin. Show or hide a product on the public Products page. */
    @PostMapping("/{id}/for-sale")
    public ProductInventory setForSale(@PathVariable String id, @RequestBody ForSaleRequest request) {
        ProductInventory p = service.read(id);
        return service.update(new ProductInventory.Builder()
                .setProductId(p.getProductId())
                .setName(p.getName())
                .setBrand(p.getBrand())
                .setCategory(p.getCategory())
                .setStockQuantity(p.getStockQuantity())
                .setReorderLevel(p.getReorderLevel())
                .setCostPrice(p.getCostPrice())
                .setSellingPrice(p.getSellingPrice())
                .setSalonService(p.getSalonService())
                .setForSale(request.forSale())
                .build());
    }

    public record AdjustStockRequest(int delta) {}

    @PostMapping
    public ProductInventory create(@RequestBody ProductRequest request) {
        var salonService = request.serviceId() == null ? null
                : salonServiceService.read(request.serviceId());
        return service.register(request.name(), request.brand(), request.category(),
                request.stockQuantity(), request.reorderLevel(), request.costPrice(),
                request.sellingPrice(), salonService);
    }

    @GetMapping("/{id}")
    public ProductInventory read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<ProductInventory> getAll() {
        return service.getAll();
    }

    @GetMapping("/low-stock")
    public List<ProductInventory> lowStock() {
        return service.lowStock();
    }

    @PostMapping("/{id}/adjust-stock")
    public ProductInventory adjustStock(@PathVariable String id, @RequestBody AdjustStockRequest request) {
        return service.adjustStock(id, request.delta());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ProductInventory update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        ProductInventory existing = service.read(id);
        var salonService = request.serviceId() == null ? null
                : salonServiceService.read(request.serviceId());
        ProductInventory updated = new ProductInventory.Builder()
                .setProductId(id)
                .setName(request.name())
                .setBrand(request.brand())
                .setCategory(request.category())
                .setStockQuantity(existing.getStockQuantity())
                .setReorderLevel(request.reorderLevel())
                .setCostPrice(Money.of(request.costPrice()))
                .setSellingPrice(Money.of(request.sellingPrice()))
                .setSalonService(salonService)
                .setForSale(existing.isForSale())   // editing details must not unlist a product
                .build();
        return service.update(updated);
    }
}