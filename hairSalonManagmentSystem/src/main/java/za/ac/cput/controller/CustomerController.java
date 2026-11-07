package za.ac.cput.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.ICustomerService;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final ICustomerService service;
    private final AuthService authService;

    public CustomerController(ICustomerService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    public record CustomerRequest(String firstName, String lastName, String email,
                                  String phoneNumber, LocalDate dateOfBirth) {

    }
    @PostMapping
    public Customer create(@RequestBody CustomerRequest request) {
        if (request.dateOfBirth() != null) {
            return service.registerWithDetails(request.firstName(), request.lastName(),
                    request.email(), request.phoneNumber(), request.dateOfBirth());
        }
        return service.register(request.firstName(), request.lastName(),
                request.email(), request.phoneNumber());
}
    /** The customer themselves, or an admin. Any other customer gets 403. */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.granted() or @access.isCustomer(#id)")
    public Customer read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Customer> getAll() {

        return service.getAll();
    }

    @GetMapping("/email/{email}")
    public Customer findByEmail(@PathVariable String email) {

        return service.findByEmail(email);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {

        service.delete(id);
    }

    /**
     * The customer edits their own profile; an admin edits anyone's. If the email changes, the
     * customer's login follows it - logins are linked to customers by email.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.granted() or @access.isCustomer(#id)")
    @Transactional
    public Customer update(@PathVariable String id, @RequestBody CustomerRequest request) {
        Customer existing = service.read(id);
        Email newEmail = Email.of(request.email());
        authService.syncLoginEmail(existing.getEmail(), newEmail);
        Customer updated = new Customer.Builder()
                .setCustomerId(id)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setEmail(newEmail)
                .setPhoneNumber(PhoneNumber.of(request.phoneNumber()))
                .setDateOfBirth(request.dateOfBirth())
                .setRegisteredAt(existing.getRegisteredAt())
                .build();
        return service.update(updated);
    }

}