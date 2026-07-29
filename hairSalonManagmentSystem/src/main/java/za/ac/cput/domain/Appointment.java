package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Appointment")
public class Appointment {

    @Id
    private String appointmentId;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

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

    protected Appointment() {}

    public Appointment(Builder builder) {
        this.appointmentId = builder.appointmentId;
        this.appointmentDate = builder.appointmentDate;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
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

    public static class Builder{
        private String appointmentId;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AppointmentStatus status;
        private String notes;
        private LocalDateTime createdAt;
        private Customer customer;
        private Stylist stylist;

        public Builder setAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder setAppointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public Builder setStartTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(LocalTime endTime) {
            this.endTime = endTime;
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

        public Appointment build() {
            return new Appointment(this);
        }
    }
}