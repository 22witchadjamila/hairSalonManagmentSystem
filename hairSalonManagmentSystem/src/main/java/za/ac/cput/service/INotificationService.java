package za.ac.cput.service;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;

import java.util.List;

public interface INotificationService extends IService<Notification, String> {
    Notification scheduleAppointmentReminder(String appointmentId, NotificationChannel channel);
    Notification sendPromotionNotification(String message, NotificationChannel channel);
    Notification notifyAppointmentConfirmed(Appointment appointment);
    List<Notification> findByCustomer(String customerId);
}
