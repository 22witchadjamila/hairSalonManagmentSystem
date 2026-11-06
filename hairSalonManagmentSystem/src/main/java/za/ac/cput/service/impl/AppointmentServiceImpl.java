package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.valueobject.TimeSlot;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.AppointmentFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.repository.SalonServiceRepository;
import za.ac.cput.repository.StylistRepository;
import za.ac.cput.service.IAppointmentService;
import za.ac.cput.service.INotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository repository;
    private final CustomerRepository customerRepository;
    private final StylistRepository stylistRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final INotificationService notificationService;
    private final PaymentRepository paymentRepository;


    public AppointmentServiceImpl(AppointmentRepository repository,
                                   CustomerRepository customerRepository,
                                   StylistRepository stylistRepository,
                                   SalonServiceRepository salonServiceRepository,
                                  INotificationService notificationService,
                                  PaymentRepository paymentRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.stylistRepository = stylistRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Appointment create(Appointment appointment) {
        return repository.save(appointment);
    }

    @Override
    public Appointment read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Appointment", id));
    }

    @Override
    public Appointment update(Appointment appointment) {
        read(appointment.getAppointmentId());
        return repository.save(appointment);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Appointment> getAll() {
        return repository.findAll();
    }

    @Override
    public Appointment bookAppointment(String customerId, String stylistId, String salonServiceId,
                                        LocalDate date, LocalTime startTime, String notes) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", customerId));
        Stylist stylist = stylistRepository.findById(stylistId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Stylist", stylistId));
        SalonService salonService = salonServiceRepository.findById(salonServiceId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("SalonService", salonServiceId));

        TimeSlot requestedSlot = TimeSlot.of(startTime, startTime.plusMinutes(salonService.getDurationMinutes()));
        boolean overlaps = repository.findByStylist_StylistIdAndAppointmentDate(stylistId, date).stream()
                .filter(existing -> existing.getStatus() != AppointmentStatus.CANCELLED)
                .anyMatch(existing -> requestedSlot.overlaps(existing.getTimeSlot()));
        if (overlaps) {
            throw new InvalidOperationException(
                    "Stylist is already booked for that time slot on " + date);
        }

        Appointment appointment = AppointmentFactory.buildAppointment(
                customer, stylist, salonService, date, startTime, notes);
        if (appointment == null) {
            throw new InvalidOperationException("Invalid appointment details provided.");
        }
        return create(appointment);
    }

    @Override
    public Appointment confirmAppointment(String appointmentId) {
        if (read(appointmentId).getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidOperationException("Only a pending appointment can be confirmed.");
        }
        Appointment confirmed = updateStatus(appointmentId, AppointmentStatus.CONFIRMED);
        notificationService.notifyAppointmentConfirmed(confirmed);
        return confirmed;
    }

    @Override
    public Appointment cancelAppointment(String appointmentId) {
        AppointmentStatus status = read(appointmentId).getStatus();
        // Done, already cancelled or no-show bookings are history; cancelling them would just rewrite it.
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidOperationException("Only a pending or confirmed appointment can be cancelled.");
        }
        // A paid booking that is cancelled would leave the money (and any promotion use or loyalty
        // points) counted against an appointment that never happened. Refund first.
        if (paymentRepository.existsByAppointment_AppointmentIdAndStatus(appointmentId, PaymentStatus.PAID)) {
            throw new InvalidOperationException("This appointment has been paid. Refund the payment first, then cancel it.");
        }
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    @Override
    public Appointment completeAppointment(String appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    @Override
    public List<Appointment> findByCustomer(String customerId) {
        return repository.findByCustomer_CustomerId(customerId);
    }
    @Override
    public List<Appointment> findByStylist(String stylistId) {
        return repository.findByStylist_StylistId(stylistId);
    }

    // Appointment has no setStatus (the domain model is immutable-by-design via Builder),
    // so status changes are done by rebuilding the entity and saving it back.
    private Appointment updateStatus(String appointmentId, AppointmentStatus newStatus) {
        Appointment existing = read(appointmentId);
        Appointment updated = new Appointment.Builder()
                .setAppointmentId(existing.getAppointmentId())
                .setCustomer(existing.getCustomer())
                .setStylist(existing.getStylist())
                .setSalonService(existing.getSalonService())
                .setAppointmentDate(existing.getAppointmentDate())
                .setTimeSlot(existing.getTimeSlot())
                .setNotes(existing.getNotes())
                .setPromoCode(existing.getPromoCode())   // keep the customer's code through status changes
                .setCreatedAt(existing.getCreatedAt())
                .setStatus(newStatus)
                .build();
        return repository.save(updated);
    }

}
