package za.ac.cput.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.IStylistService;
import za.ac.cput.service.RetirementService;

import java.util.List;

@RestController
@RequestMapping("/api/stylists")
public class StylistController {

    private final IStylistService service;
    private final RetirementService retirementService;
    private final AuthService authService;

    public StylistController(IStylistService service, RetirementService retirementService,
                             AuthService authService) {
        this.service = service;
        this.retirementService = retirementService;
        this.authService = authService;
    }

    public record StylistRequest(String firstName, String lastName, String email,
                                 String phoneNumber, String speciality) {}

    /** What the public site may know about a stylist - no email address or phone number. */
    public record PublicStylist(String stylistId, String firstName, String lastName, String speciality) {
        static PublicStylist of(Stylist s) {
            return new PublicStylist(s.getStylistId(), s.getFirstName(), s.getLastName(), s.getSpeciality());
        }
    }

    @PostMapping
    @PreAuthorize("@perm.granted()")
    public Stylist create(@RequestBody StylistRequest request) {
        return service.register(request.firstName(), request.lastName(),
                request.email(), request.phoneNumber(), request.speciality());
    }

    /** Full record - the stylist themselves, or an admin. */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.granted() or @access.isStylist(#id)")
    public Stylist read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    @PreAuthorize("@perm.granted()")
    public List<Stylist> getAll() {
        return service.getAll();
    }

    /**
     * Public: the booking page and the "Meet the team" page. Returns a projection - this used to
     * return the full entity, publishing every stylist's email and phone number to anyone.
     */
    @GetMapping("/active")
    public List<PublicStylist> findActive() {
        return service.findActiveStylists().stream().map(PublicStylist::of).toList();
    }

    /** Deletes an unused stylist, archives one with appointments - see RetirementService. */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.granted()")
    public RetirementService.Result delete(@PathVariable String id) {
        return retirementService.retireStylist(id);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("@perm.granted()")
    public Stylist restore(@PathVariable String id) {
        return retirementService.restoreStylist(id);
    }

    /**
     * A stylist edits their own profile; an admin edits anyone's. Active status is never changed
     * here - that is archive/restore. If the email changes, the stylist's login follows it,
     * because logins are linked to stylists by email.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.granted() or @access.isStylist(#id)")
    @Transactional
    public Stylist update(@PathVariable String id, @RequestBody StylistRequest request) {
        Stylist existing = service.read(id);
        Email newEmail = Email.of(request.email());
        authService.syncLoginEmail(existing.getEmail(), newEmail);
        Stylist updated = new Stylist.Builder()
                .setStylistId(id)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setEmail(newEmail)
                .setPhoneNumber(PhoneNumber.of(request.phoneNumber()))
                .setSpeciality(request.speciality())
                .setActive(existing.isActive())
                .build();
        return service.update(updated);
    }
}
