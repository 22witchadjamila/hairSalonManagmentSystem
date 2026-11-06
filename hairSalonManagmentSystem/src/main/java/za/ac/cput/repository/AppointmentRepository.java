package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByStylist_StylistIdAndAppointmentDate(String stylistId, LocalDate appointmentDate);
    List<Appointment> findByCustomer_CustomerId(String customerId);
    List<Appointment> findByStylist_StylistId(String stylistId);

    /** Used to decide between deleting and archiving a service or stylist. */
    boolean existsBySalonService_ServiceId(String serviceId);

    boolean existsByStylist_StylistId(String stylistId);

    /** Confirmed bookings in a date window - what the reminder job looks at. */
    List<Appointment> findByAppointmentDateBetweenAndStatus(LocalDate from, LocalDate to,
                                                            za.ac.cput.domain.enums.AppointmentStatus status);

    /**
     * Bookings holding a promotion code that have not been paid (or refunded) yet, grouped by
     * upper-cased code: [code, count]. Cancelled and no-show bookings never hold a use.
     */
    @org.springframework.data.jpa.repository.Query(
            "select upper(a.promoCode), count(a) from Appointment a "
            + "where a.promoCode is not null and a.status in :holding "
            + "and not exists (select p from Payment p where p.appointment = a and p.status in :settled) "
            + "group by upper(a.promoCode)")
    java.util.List<Object[]> countUnpaidBookingsByPromoCode(
            @org.springframework.data.repository.query.Param("holding") java.util.Collection<za.ac.cput.domain.enums.AppointmentStatus> holding,
            @org.springframework.data.repository.query.Param("settled") java.util.Collection<za.ac.cput.domain.enums.PaymentStatus> settled);
}
