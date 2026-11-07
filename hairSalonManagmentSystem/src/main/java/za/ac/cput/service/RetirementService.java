package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.Stylist;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.ProductInventoryRepository;
import za.ac.cput.repository.SalonServiceRepository;
import za.ac.cput.repository.StylistRepository;

/**
 * The deletion strategy for catalogue records - services and stylists.
 *
 * Both are referenced by appointments (foreign keys service_id and stylist_id). Hard-deleting
 * one that has bookings is refused by the database, and forcing it with a cascade would erase
 * booking and payment history. So:
 *
 *   never referenced -> DELETED   (removed outright)
 *   referenced       -> ARCHIVED  (isActive = false: hidden from the public site and from new
 *                                  bookings; existing appointments keep working)
 *
 * Archived records can be restored. The admin is told which of the two happened.
 */
@Service
public class RetirementService {

    public enum Outcome { DELETED, ARCHIVED }

    public record Result(Outcome outcome, String message) {}

    private final SalonServiceRepository services;
    private final StylistRepository stylists;
    private final AppointmentRepository appointments;
    private final ProductInventoryRepository products;

    public RetirementService(SalonServiceRepository services, StylistRepository stylists,
                             AppointmentRepository appointments, ProductInventoryRepository products) {
        this.services = services;
        this.stylists = stylists;
        this.appointments = appointments;
        this.products = products;
    }

    // ---------------------------------------------------------------- services

    @Transactional
    public Result retireService(String serviceId) {
        SalonService service = services.findById(serviceId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Service", serviceId));

        if (!appointments.existsBySalonService_ServiceId(serviceId)
                && !products.existsBySalonService_ServiceId(serviceId)) {
            services.delete(service);
            return new Result(Outcome.DELETED, service.getName() + " was deleted.");
        }
        services.save(withActive(service, false));
        return new Result(Outcome.ARCHIVED, service.getName()
                + " has existing bookings, so it was archived instead of deleted. "
                + "It no longer appears to customers, and its booking history is kept.");
    }

    @Transactional
    public SalonService restoreService(String serviceId) {
        SalonService service = services.findById(serviceId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Service", serviceId));
        return services.save(withActive(service, true));
    }

    private static SalonService withActive(SalonService s, boolean active) {
        return new SalonService.Builder()
                .setServiceId(s.getServiceId())
                .setName(s.getName())
                .setDescription(s.getDescription())
                .setDurationMinutes(s.getDurationMinutes())
                .setPrice(s.getPrice())
                .setCategory(s.getCategory())
                .setActive(active)
                .build();
    }

    // ---------------------------------------------------------------- stylists

    @Transactional
    public Result retireStylist(String stylistId) {
        Stylist stylist = stylists.findById(stylistId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Stylist", stylistId));
        String name = stylist.getFirstName() + " " + stylist.getLastName();

        if (!appointments.existsByStylist_StylistId(stylistId)) {
            stylists.delete(stylist);
            return new Result(Outcome.DELETED, name + " was deleted.");
        }
        stylists.save(withActive(stylist, false));
        return new Result(Outcome.ARCHIVED, name
                + " has existing appointments, so they were archived instead of deleted. "
                + "They can no longer be booked, and their appointment history is kept.");
    }

    @Transactional
    public Stylist restoreStylist(String stylistId) {
        Stylist stylist = stylists.findById(stylistId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Stylist", stylistId));
        return stylists.save(withActive(stylist, true));
    }

    private static Stylist withActive(Stylist s, boolean active) {
        return new Stylist.Builder()
                .setStylistId(s.getStylistId())
                .setFirstName(s.getFirstName())
                .setLastName(s.getLastName())
                .setEmail(s.getEmail())
                .setPhoneNumber(s.getPhoneNumber())
                .setSpeciality(s.getSpeciality())
                .setActive(active)
                .build();
    }
}
