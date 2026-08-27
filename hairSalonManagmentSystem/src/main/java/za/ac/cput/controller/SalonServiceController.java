package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.service.ISalonServiceService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class SalonServiceController {

    private final ISalonServiceService service;

    public SalonServiceController(ISalonServiceService service) {
        this.service = service;
    }

    public record SalonServiceRequest(String name, String description, int durationMinutes,
                                      BigDecimal price, ServiceCategory category) {}

    @PostMapping
    public SalonService create(@RequestBody SalonServiceRequest request) {
        return service.register(request.name(), request.description(),
                request.durationMinutes(), request.price(), request.category());
    }

    @GetMapping("/{id}")
    public SalonService read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<SalonService> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public List<SalonService> findActive() {
        return service.findActiveServices();
    }

    @GetMapping("/category/{category}")
    public List<SalonService> findByCategory(@PathVariable ServiceCategory category) {
        return service.findByCategory(category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
