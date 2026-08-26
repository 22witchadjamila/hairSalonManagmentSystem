package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.ProductInventory;
import za.ac.cput.service.IProductInventoryService;
import za.ac.cput.service.ISalonServiceService;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductInventoryController {

    private final IProductInventoryService service;
    private final ISalonServiceService salonServiceService;

    public ProductInventoryController(IProductInventoryService service,
                                      ISalonServiceService salonServiceService) {
        this.service = service;
        this.salonServiceService = salonServiceService;
    }

    public record ProductRequest(String name, String brand, String category, int stockQuantity,
                                 int reorderLevel, BigDecimal costPrice, BigDecimal sellingPrice,
                                 String salonServiceId) {}

    public record AdjustStockRequest(int delta) {}

    @PostMapping
    public ProductInventory create(@RequestBody ProductRequest request) {
        var salonService = request.salonServiceId() == null ? null
                : salonServiceService.read(request.salonServiceId());
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
}
