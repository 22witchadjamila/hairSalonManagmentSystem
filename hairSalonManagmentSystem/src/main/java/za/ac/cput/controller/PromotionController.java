package za.ac.cput.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.service.IPromotionService;
import za.ac.cput.service.PromotionAdminService;
import za.ac.cput.service.PromotionOfferService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final IPromotionService service;
    private final PromotionOfferService offers;
    private final PromotionAdminService admin;

    public PromotionController(IPromotionService service, PromotionOfferService offers,
                               PromotionAdminService admin) {
        this.service = service;
        this.offers = offers;
        this.admin = admin;
    }

    public record PromotionRequest(String code, String description, DiscountType discountType,
                                   BigDecimal discountValue, LocalDate startDate,
                                   LocalDate endDate, Integer usageLimit) {
        PromotionAdminService.Details details() {
            return new PromotionAdminService.Details(code, description, discountType, discountValue,
                    startDate, endDate, usageLimit);
        }
    }

    /** Admin. Validated in PromotionAdminService; codes are stored upper-case. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion create(@RequestBody PromotionRequest request) {
        return admin.create(request.details());
    }

    @GetMapping("/{id}")
    public Promotion read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Promotion> getAll() {
        return service.getAll();
    }

    /**
     * Admin. Usage figures computed from the payment and booking rows - what the Promotions page
     * shows as "times used" and "booked, not yet paid". See PromotionOfferService for the policy.
     */
    @GetMapping("/stats")
    public List<PromotionOfferService.UsageStats> stats() {
        return offers.usageStats();
    }

    /** Admin. Edits details only; the usage count is never written by an edit. */
    @PutMapping("/{id}")
    public Promotion update(@PathVariable String id, @RequestBody PromotionRequest request) {
        return admin.update(id, request.details());
    }

    /** Admin. End a promotion before its end date. History is kept; it can be reopened. */
    @PostMapping("/{id}/end")
    public Promotion end(@PathVariable String id) {
        return admin.end(id);
    }

    @PostMapping("/{id}/reopen")
    public Promotion reopen(@PathVariable String id) {
        return admin.reopen(id);
    }

    /** Public. Promotions a customer can use today - code, description, discount, end date. */
    @GetMapping("/available")
    public List<PromotionOfferService.Offer> available() {
        return offers.available();
    }

    /** Public. Server-calculated price preview for a service with a code. */
    @GetMapping("/quote")
    public PromotionOfferService.Quote quote(@RequestParam String code, @RequestParam String serviceId) {
        return offers.quote(code, serviceId);
    }

    @GetMapping("/validate/{code}")
    public Promotion validate(@PathVariable String code) {
        return service.validate(code);
    }

    /** Refused for a promotion that has been used or booked - end it instead. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        admin.delete(id);
    }


}

