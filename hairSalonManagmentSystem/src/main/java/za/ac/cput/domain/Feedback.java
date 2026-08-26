package za.ac.cput.domain;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="reviews")
public class Feedback {

    @Id
    private String reviewId;
    private int rating;
    private String comment;
    private LocalDateTime submittedAt;
    private boolean isVerified;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    protected Feedback() {}

    public Feedback(Builder builder) {
        this.reviewId    = builder.reviewId;
        this.rating      = builder.rating;
        this.comment     = builder.comment;
        this.submittedAt = builder.submittedAt;
        this.isVerified  = builder.isVerified;
        this.appointment = builder.appointment;
        this.customer    = builder.customer;
    }

    public String getReviewId()          { return reviewId; }
    public int getRating()               { return rating; }
    public String getComment()           { return comment; }
    public LocalDateTime getSubmittedAt(){ return submittedAt; }
    public boolean isVerified()          { return isVerified; }
    public Appointment getAppointment()  { return appointment; }
    public Customer getCustomer()        { return customer; }

    public static class Builder {
        private String reviewId;
        private int rating;
        private String comment;
        private LocalDateTime submittedAt;
        private boolean isVerified;
        private Appointment appointment;
        private Customer customer;

        public Builder setReviewId(String reviewId) {
            this.reviewId = reviewId; return this;
        }
        public Builder setRating(int rating) {
            this.rating = rating; return this;
        }
        public Builder setComment(String comment) {
            this.comment = comment; return this;
        }
        public Builder setSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt; return this;
        }
        public Builder setVerified(boolean isVerified) {
            this.isVerified = isVerified; return this;
        }
        public Builder setAppointment(Appointment appointment) {
            this.appointment = appointment; return this;
        }
        public Builder setCustomer(Customer customer) {
            this.customer = customer; return this;
        }
        public Feedback build() {
            return new Feedback(this);
        }
    }
}
