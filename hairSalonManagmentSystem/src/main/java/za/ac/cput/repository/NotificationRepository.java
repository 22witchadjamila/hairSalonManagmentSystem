package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByAppointment_AppointmentId(String appointmentId);
    List<Notification> findByAppointment_Customer_CustomerIdOrderByScheduledAtDesc(String customerId);
}
