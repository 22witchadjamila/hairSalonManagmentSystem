package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByStylist_StylistIdAndAppointmentDate(String stylistId, LocalDate appointmentDate);
    List<Appointment> findByCustomer_CustomerId(String customerId);
    List<Appointment> findByStylist_StylistId(String stylistId);
}
