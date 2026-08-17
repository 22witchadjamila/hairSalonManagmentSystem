package za.ac.cput.service;

import za.ac.cput.domain.Feedback;

public interface IFeedbackService extends IService<Feedback, String> {
    Feedback submitFeedback(String appointmentId, String customerId,
                            int rating, String comment);
}
