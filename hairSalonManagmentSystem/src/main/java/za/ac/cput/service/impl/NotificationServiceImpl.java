package za.ac.cput.service.impl;

import za.ac.cput.domain.Notification;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.NotificationRepository;
import za.ac.cput.service.INotificationService;

public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository repository;
    private final AppointmentRepository appointmentRepository;

    public NotificationServiceImpl(NotificationRepository repository, AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Notification create(Notification notification){
        return repository.save(notification);
    }

    @Override
    public Notification read(String id){
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Notification", id));
    }

    @Override
    public Notification update(Notification notification){
        read(notification.getNotificationId());
        return repository.save(notification);
    }

    @Override
    public void delete(String id){
        repository.deleteById(id);
    }
}
