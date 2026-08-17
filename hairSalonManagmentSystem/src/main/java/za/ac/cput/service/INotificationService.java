package za.ac.cput.service;

import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;

public interface INotificationService extends IService<Notification, String>{
    Notification scheduleAppointmentReminder(String appointmentId, NotificationChannel channel);
    Notification sendPromotionNotification(String message, NotificationChannel channel);
}
