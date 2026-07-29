package za.ac.cput.domain;


import jakarta.persistence.*;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.domain.enums.NotificationStatus;
import za.ac.cput.domain.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    private String notificationId;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    protected Notification() {}

    public Notification(Builder builder) {
        this.notificationId = builder.notificationId;
        this.message        = builder.message;
        this.type           = builder.type;
        this.channel        = builder.channel;
        this.status         = builder.status;
        this.scheduledAt    = builder.scheduledAt;
        this.sentAt         = builder.sentAt;
        this.appointment    = builder.appointment;

    }

    public String getNotificationId() {
        return notificationId;
    }
    public String getMessage() {
        return message;
    }
    public NotificationType getType() {
        return type;
    }
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public NotificationChannel getChannel() {return channel;}
    public NotificationStatus getStatus() {return status;}
    public Appointment getAppointment() {return appointment;}

    public static class Builder{

        private String notificationId;
        private String message;
        private NotificationType type;
        private NotificationChannel channel;
        private NotificationStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime sentAt;
        private Appointment appointment;


        public Builder setNotificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setType(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder setScheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder setSentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder setAppointment(Appointment appointment) {
            this.appointment = appointment;
            return this;
        }

        public Builder setStatus(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public Builder setChannel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public Notification build(){
            return new Notification(this);
        }
    }
}
