package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Appointment")
public class Appointment {

    @Id
    private String appointmentId;

    private LocalDate appointmentDate;

    @Embedded
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String notes;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "stylist_id")
    private Stylist stylist;

    @ManyToOne
    @JoinColumn(name = "stylist_id")
    private SalonService salonService;


    protected Appointment() {}

    public Appointment(Builder builder) {
        this.appointmentId = builder.appointmentId;
        this.appointmentDate = builder.appointmentDate;
        this.timeSlot = builder.timeSlot;
        this.status = builder.status;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
        this.customer = builder.customer;
        this.stylist = builder.stylist;
        this.salonService = builder.salonService;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }


    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Stylist getStylist() {
        return stylist;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public TimeSlot getTimeSlot() {return timeSlot;}

    public SalonService getSalonService() {return salonService;}

    public LocalTime getStartTime() {
        return timeSlot.getStartTime();
    }

    public LocalTime getEndTime() {
        return timeSlot.getEndTime();
    }

    public static class Builder{
        private String appointmentId;
        private LocalDate appointmentDate;
        private TimeSlot timeSlot;
        private AppointmentStatus status;
        private String notes;
        private LocalDateTime createdAt;
        private Customer customer;
        private Stylist stylist;
        private SalonService salonService;

        public Builder setAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder setAppointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public Builder setTimeSlot(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setStatus(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder setCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder setStylist(Stylist stylist) {
            this.stylist = stylist;
            return this;
        }

        public Builder setSalonService(SalonService salonService) {
            this.salonService = salonService;
            return this;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}