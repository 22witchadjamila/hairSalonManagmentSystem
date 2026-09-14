package za.ac.cput.service;

import za.ac.cput.domain.Feedback;

import java.util.List;

public interface IFeedbackService extends IService<Feedback, String> {
    Feedback submitFeedback(String appointmentId, String customerId, int rating, String comment);
    List<Feedback> findByCustomer(String customerId);
}
