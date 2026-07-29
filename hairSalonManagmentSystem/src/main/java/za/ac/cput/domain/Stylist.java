/* Stylist.java
- Domain model class representing a Stylist entity with attributes and Builder pattern.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/

package za.ac.cput.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "stylists")
public class Stylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String stylistId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String specialty;
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "appointment_appointment_id")
    private Appointment appointment;

    public Appointment getAppointment() {
        return appointment;
    }


    public Stylist() {}

    public Stylist(Builder builder) {
        this.stylistId = builder.stylistId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.specialty = builder.specialty;
        this.isActive = builder.isActive;
    }

    public String getStylistId() {
        return stylistId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSpeciality() {
        return specialty;
    }

    public boolean isActive() {
        return isActive;
    }

    public static class Builder{
        private String stylistId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String specialty;
        private boolean isActive;

        public Builder setStylistId(String stylistId) {
            this.stylistId = stylistId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }


        public Builder setSpeciality(String speciality) {
            this.specialty = speciality;
            return this;
        }

        public Builder setActive(boolean active) {
            this.isActive = active;
            return this;
        }

        public Stylist build() {
            return new Stylist();
        }

    }
}