package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.domain.enums.NotificationStatus;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.util.Helper;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class NotificationFactory {

    public static Notification buildAppointmentReminder(Appointment appointment,
                                                        NotificationChannel channel){
        if(appointment == null || channel == null) return null;

        String message = "Reminder: You have an appointment on "
                + appointment.getAppointmentDate()
                + " at " + appointment.getStartTime()
                + ". Please arrive 5 minutes early.";

        LocalDateTime scheduleAt = LocalDateTime.of(
                appointment.getAppointmentDate().minusDays(1),
                LocalTime.of(9, 0));

        return new Notification.Builder()
                .setNotificationId(Helper.generateId())
                .setMessage(message)
                .setType(NotificationType.APPOINTMENT_REMINDER)
                .setChannel(channel)
                .setStatus(NotificationStatus.SCHEDULED)
                .setScheduledAt(scheduleAt)
                .setAppointment(appointment)
                .build();
    }

    public static Notification buildPromotionNotification(String message,
                                                          NotificationChannel channel){
        if (Helper.isNullOrEmpty(message)) return null;
        if (channel == null) return null;

        return new Notification.Builder()
                .setNotificationId(Helper.generateId())
                .setMessage(message)
                .setType(NotificationType.PROMOTION)
                .setChannel(channel)
                .setStatus(NotificationStatus.SCHEDULED)
                .setScheduledAt(LocalDateTime.now())
                .build();
    }
    public static Notification buildAppointmentConfirmed(Appointment appointment) {
        if (appointment == null) return null;
        String message = "Good news! Your " + appointment.getSalonService().getName()
                + " appointment on " + appointment.getAppointmentDate()
                + " at " + appointment.getStartTime() + " has been confirmed.";
        return new Notification.Builder()
                .setNotificationId(Helper.generateId())
                .setMessage(message)
                .setType(NotificationType.APPOINTMENT_CONFIRMED)
                .setChannel(NotificationChannel.IN_APP)
                .setStatus(NotificationStatus.SENT)
                .setScheduledAt(LocalDateTime.now())
                .setSentAt(LocalDateTime.now())
                .setAppointment(appointment)
                .build();
    }
}
