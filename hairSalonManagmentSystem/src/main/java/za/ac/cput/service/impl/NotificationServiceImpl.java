package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.domain.enums.NotificationStatus;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.NotificationFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.NotificationRepository;
import za.ac.cput.service.INotificationService;
import za.ac.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private static final int MAX_PROMOTION_LENGTH = 280;
    private static final int ANNOUNCEMENT_DAYS = 30;

    private final NotificationRepository repository;
    private final AppointmentRepository appointmentRepository;

    public NotificationServiceImpl(NotificationRepository repository,
                                    AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Notification create(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Notification read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Notification", id));
    }

    @Override
    public Notification update(Notification notification) {
        read(notification.getNotificationId());
        return repository.save(notification);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Notification> getAll() {
        return repository.findAll();
    }

    @Override
    public Notification scheduleAppointmentReminder(String appointmentId, NotificationChannel channel) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Appointment", appointmentId));

        Notification notification = NotificationFactory.buildAppointmentReminder(appointment, channel);
        if (notification == null) {
            throw new InvalidOperationException("Could not schedule reminder for this appointment.");
        }
        return create(notification);
    }

    /**
     * A promotion message is an ANNOUNCEMENT: it has no appointment, so every customer sees it in
     * their notification bell for the next 30 days. In-app is the only channel that is really
     * connected - nothing sends email, SMS or push - so the others are refused rather than saved
     * as "scheduled" rows that would never go anywhere.
     */
    @Override
    public Notification sendPromotionNotification(String message, NotificationChannel channel) {
        NotificationChannel used = channel == null ? NotificationChannel.IN_APP : channel;
        if (used != NotificationChannel.IN_APP) {
            throw new InvalidOperationException(
                    "Email, SMS and push aren't connected yet. Promotions are shown in customers' notification bell.");
        }
        String text = message == null ? "" : message.trim();
        if (text.length() > MAX_PROMOTION_LENGTH) {
            throw new InvalidOperationException("Keep the message to " + MAX_PROMOTION_LENGTH + " characters or fewer.");
        }
        Notification built = NotificationFactory.buildPromotionNotification(text, used);
        if (built == null) {
            throw new InvalidOperationException("Invalid notification details provided.");
        }
        LocalDateTime now = LocalDateTime.now();
        return create(new Notification.Builder()
                .setNotificationId(built.getNotificationId())
                .setMessage(built.getMessage())
                .setType(built.getType())
                .setChannel(built.getChannel())
                .setStatus(NotificationStatus.SENT)   // visible immediately, so it is sent, not "scheduled"
                .setScheduledAt(now)
                .setSentAt(now)
                .build());
    }
    @Override
    public Notification notifyAppointmentConfirmed(Appointment appointment) {
        Notification notification = NotificationFactory.buildAppointmentConfirmed(appointment);
        if (notification == null) {
            throw new InvalidOperationException(
                    "Could not build a confirmation notification for this appointment.");
        }
        return create(notification);
    }
    @Override
    public Notification notifyAppointmentCancelled(Appointment appointment) {
        Notification notification = NotificationFactory.buildAppointmentCancelled(appointment);
        if (notification == null) {
            throw new InvalidOperationException("Could not build a cancellation notification for this appointment.");
        }
        return create(notification);
    }

    @Override
    public List<Notification> findByCustomer(String customerId) {
        return repository.findVisibleTo(customerId, NotificationType.PROMOTION, NotificationStatus.SENT,
                LocalDateTime.now().minusDays(ANNOUNCEMENT_DAYS));
    }

    @Override
    @Transactional
    public int sendDueReminders(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        int created = 0;
        for (Appointment a : appointmentRepository.findByAppointmentDateBetweenAndStatus(
                today, today.plusDays(1), AppointmentStatus.CONFIRMED)) {
            // Past appointments don't need reminding; neither do ones already reminded.
            if (a.getAppointmentDate().atTime(a.getStartTime()).isBefore(now)) continue;
            if (repository.existsByAppointment_AppointmentIdAndType(a.getAppointmentId(), NotificationType.APPOINTMENT_REMINDER)) continue;
            String day = a.getAppointmentDate().equals(today) ? "today" : "tomorrow";
            String stylist = a.getStylist() == null ? "" : " with " + a.getStylist().getFirstName();
            repository.save(new Notification.Builder()
                    .setNotificationId(Helper.generateId())
                    .setMessage("Reminder: your " + a.getSalonService().getName() + " appointment" + stylist + " is "
                            + day + " at " + a.getStartTime() + ". Please arrive 5 minutes early.")
                    .setType(NotificationType.APPOINTMENT_REMINDER)
                    .setChannel(NotificationChannel.IN_APP)
                    .setStatus(NotificationStatus.SENT)
                    .setScheduledAt(now)
                    .setSentAt(now)
                    .setAppointment(a)
                    .build());
            created++;
        }
        return created;
    }
    }

