package za.ac.cput.service;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;

public interface INotificationService extends IService<Notification, String> {
    Notification scheduleAppointmentReminder(String appointmentId, NotificationChannel channel);
    Notification sendPromotionNotification(String message, NotificationChannel channel);
    Notification notifyAppointmentConfirmed(Appointment appointment);

    /** Tells the customer their booking was cancelled by the salon (a stylist or an admin). */
    Notification notifyAppointmentCancelled(Appointment appointment);
    List<Notification> findByCustomer(String customerId);

    /**
     * Creates an in-app reminder for every CONFIRMED appointment that is today (and still ahead)
     * or tomorrow and has none yet. Safe to call as often as you like: one reminder per appointment.
     * @return how many reminders were created
     */
    int sendDueReminders(LocalDateTime now);
}
