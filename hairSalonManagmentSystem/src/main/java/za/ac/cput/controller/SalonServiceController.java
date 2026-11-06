package za.ac.cput.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.service.ISalonServiceService;
import za.ac.cput.service.RetirementService;
import za.ac.cput.domain.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class SalonServiceController {

    private final ISalonServiceService service;
    private final RetirementService retirementService;

    public SalonServiceController(ISalonServiceService service, RetirementService retirementService) {
        this.service = service;
        this.retirementService = retirementService;
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


    /**
     * Deletes an unused service; archives one that has bookings (see RetirementService), and
     * says which happened. A plain delete of a booked service used to fail on the database's
     * foreign key and surface as an error banner.
     */
    @DeleteMapping("/{id}")
    public RetirementService.Result delete(@PathVariable String id) {
        return retirementService.retireService(id);
    }

    /** Undo an archive: the service is bookable and visible to customers again. */
    @PostMapping("/{id}/restore")
    public SalonService restore(@PathVariable String id) {
        return retirementService.restoreService(id);
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