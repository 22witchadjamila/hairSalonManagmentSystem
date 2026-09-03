package za.ac.cput.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.service.ISalonServiceService;
import za.ac.cput.domain.valueobject.Money;
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

@PutMapping("/{id}")
public SalonService update(@PathVariable String id, @Valid @RequestBody SalonServiceRequest request) {
    SalonService existing = service.read(id);
    SalonService updated = new SalonService.Builder()
            .setServiceId(id)
            .setName(request.name())
            .setDescription(request.description())
            .setDurationMinutes(request.durationMinutes())
            .setPrice(Money.of(request.price()))
            .setCategory(request.category())
            .setActive(existing.isActive())
            .build();
    return service.update(updated);
}
}