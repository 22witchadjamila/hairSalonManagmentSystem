package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Feedback;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.FeedbackFactory;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.repository.FeedbackRepository;
import za.ac.cput.service.IFeedbackService;

import java.util.List;

@Service
public class FeedbackServiceImpl implements IFeedbackService {

    private final FeedbackRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;

    public FeedbackServiceImpl(FeedbackRepository repository,
                                AppointmentRepository appointmentRepository,
                                CustomerRepository customerRepository) {
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Feedback create(Feedback feedback) {
        return repository.save(feedback);
    }

    @Override
    public Feedback read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Feedback", id));
    }

    @Override
    public Feedback update(Feedback feedback) {
        read(feedback.getReviewId());
        return repository.save(feedback);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Feedback> getAll() {
        return repository.findAll();
    }

    @Override
    public Feedback submitFeedback(String appointmentId, String customerId, int rating, String comment) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Appointment", appointmentId));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", customerId));

        Feedback feedback = FeedbackFactory.buildFeedback(appointment, customer, rating, comment);
        if (feedback == null) {
            throw new InvalidOperationException(
                    "Feedback can only be submitted for a completed appointment, with a rating from 1-5.");
        }
        return create(feedback);
    }
}
