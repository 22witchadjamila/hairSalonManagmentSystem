package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByAppointment_AppointmentId(String appointmentId);

    /** Duplicate guard: an appointment can be paid once. */
    boolean existsByAppointment_AppointmentIdAndStatus(String appointmentId, za.ac.cput.domain.enums.PaymentStatus status);

    /** All payments for one appointment, newest first - there can be a refunded one and a new one. */
    java.util.List<Payment> findByAppointment_AppointmentIdOrderByPaidAtDesc(String appointmentId);

    /** [promotionId, count] for every promotion with payments in this status. */
    @org.springframework.data.jpa.repository.Query(
            "select p.promotion.promotionId, count(p) from Payment p "
            + "where p.status = :status and p.promotion is not null group by p.promotion.promotionId")
    java.util.List<Object[]> countByPromotionAndStatus(
            @org.springframework.data.repository.query.Param("status") za.ac.cput.domain.enums.PaymentStatus status);
}
