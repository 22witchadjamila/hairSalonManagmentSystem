package za.ac.cput.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import za.ac.cput.service.INotificationService;

import java.time.LocalDateTime;

/**
 * Creates appointment reminders in the background. Every 30 minutes (and shortly after startup, so
 * a restart doesn't leave a gap) it gives each CONFIRMED appointment that is today or tomorrow one
 * in-app reminder, which the customer sees in their notification bell.
 *
 * Running often, rather than once at 08:00, means a booking confirmed late the day before - or this
 * morning - still gets its reminder, and a reminder isn't lost if the app happened to be down at
 * one particular time. Duplicates are impossible: the service checks before creating.
 */
@Configuration
@EnableScheduling
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final INotificationService notifications;

    public ReminderScheduler(INotificationService notifications) {
        this.notifications = notifications;
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 30 * 60 * 1000)
    public void sendReminders() {
        try {
            int created = notifications.sendDueReminders(LocalDateTime.now());
            if (created > 0) log.info("Created {} appointment reminder(s).", created);
        } catch (Exception e) {
            // A failed run must not stop the schedule: it simply tries again in 30 minutes.
            log.warn("Appointment reminders could not be created this time: {}", e.getMessage());
        }
    }
}
