package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationChannel;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.NotificationFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.NotificationRepository;
import za.ac.cput.service.INotificationService;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

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

    @Override
    public Notification sendPromotionNotification(String message, NotificationChannel channel) {
        Notification notification = NotificationFactory.buildPromotionNotification(message, channel);
        if (notification == null) {
            throw new InvalidOperationException("Invalid notification details provided.");
        }
        return create(notification);
    }
}
