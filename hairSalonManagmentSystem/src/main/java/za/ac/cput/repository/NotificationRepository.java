package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Notification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.domain.enums.NotificationStatus;
import za.ac.cput.domain.enums.NotificationType;

import java.time.LocalDateTime;


import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByAppointment_AppointmentId(String appointmentId);
    List<Notification> findByAppointment_Customer_CustomerIdOrderByScheduledAtDesc(String customerId);

    /** One reminder per appointment: the scheduler asks this before creating another. */
    boolean existsByAppointment_AppointmentIdAndType(String appointmentId, NotificationType type);

    /**
     * Everything a customer should see: messages about THEIR appointments, plus announcements
     * (a promotion message with no appointment is for every customer). Left joins, so
     * announcements - which have no appointment - are not dropped by the customer match.
     * Announcements older than `since` are left out, so a new customer isn't shown last year's offer.
     */
    @Query("select n from Notification n left join n.appointment a left join a.customer c "
            + "where c.customerId = :customerId "
            + "or (a is null and n.type = :announcement and n.status = :sent and n.scheduledAt >= :since) "
            + "order by n.scheduledAt desc")
    List<Notification> findVisibleTo(@Param("customerId") String customerId,
                                     @Param("announcement") NotificationType announcement,
                                     @Param("sent") NotificationStatus sent,
                                     @Param("since") LocalDateTime since);
}
