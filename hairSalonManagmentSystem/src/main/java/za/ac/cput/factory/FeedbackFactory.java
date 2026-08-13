package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Feedback;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.util.Helper;

import java.time.LocalDateTime;
import java.util.UUID;

public class FeedbackFactory {

    public static Feedback buildFeedback(Appointment appointment,
                                         Customer customer,
                                         int rating, String comment) {
        if(appointment == null || customer == null) return null;
        if(appointment.getStatus() != AppointmentStatus.COMPLETED) return null;
        if(rating < 1 || rating > 5) return null;

        return new Feedback.Builder()
                .setReviewId(Helper.generateId())
                .setAppointment(appointment)
                .setCustomer(customer)
                .setRating(rating)
                .setComment(comment)
                .setSubmittedAt(LocalDateTime.now())
                .setVerified(false)
                .build();
    }
}
