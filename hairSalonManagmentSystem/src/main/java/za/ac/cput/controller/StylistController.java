package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;
import za.ac.cput.service.IStylistService;

import java.util.List;

@RestController
@RequestMapping("/api/stylists")
public class StylistController {

    private final IStylistService service;

    public StylistController(IStylistService service) {
        this.service = service;
    }

    public record StylistRequest(String firstName, String lastName, String email,
                                 String phoneNumber, String speciality) {}

    @PostMapping
    public Stylist create(@RequestBody StylistRequest request) {
        return service.register(request.firstName(), request.lastName(),
                request.email(), request.phoneNumber(), request.speciality());
    }

    @GetMapping("/{id}")
    public Stylist read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Stylist> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public List<Stylist> findActive() {
        return service.findActiveStylists();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public Stylist update(@PathVariable String id, @RequestBody StylistRequest request) {
        Stylist existing = service.read(id);
        Stylist updated = new Stylist.Builder()
                .setStylistId(id)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setEmail(Email.of(request.email()))
                .setPhoneNumber(PhoneNumber.of(request.phoneNumber()))
                .setSpeciality(request.speciality())
                .setActive(existing.isActive())
                .build();
        return service.update(updated);
    }
}
