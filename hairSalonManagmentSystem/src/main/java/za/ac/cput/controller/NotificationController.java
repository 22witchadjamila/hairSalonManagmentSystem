package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.service.INotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService service;

    public NotificationController(INotificationService service) {
        this.service = service;
    }

    public record AppointmentReminderRequest(String appointmentId, NotificationChannel channel) {}
    public record PromotionNotificationRequest(String message, NotificationChannel channel) {}

    @PostMapping("/appointment-reminder")
    public Notification scheduleReminder(@RequestBody AppointmentReminderRequest request) {
        return service.scheduleAppointmentReminder(request.appointmentId(), request.channel());
    }

    @PostMapping("/promotion")
    public Notification sendPromotion(@RequestBody PromotionNotificationRequest request) {
        return service.sendPromotionNotification(request.message(), request.channel());
    }

    @GetMapping("/{id}")
    public Notification read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Notification> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
