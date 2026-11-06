/* Stylist.java
- Domain model class representing a Stylist entity with attributes and Builder pattern.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/

package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;

@Entity
@Table(name = "stylists")
public class Stylist {

    @Id
    private String stylistId;
    private String firstName;
    private String lastName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number"))
    private PhoneNumber phoneNumber;

    /** Spelled like its getter, builder and JSON. The DB column keeps its original name. */
    @Column(name = "specialty")
    private String speciality;
    private boolean isActive;

    public Stylist() {}

    public Stylist(Builder builder) {
        this.stylistId = builder.stylistId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.speciality = builder.speciality;
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

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public String getSpeciality() {
        return speciality;
    }

    public boolean isActive() {
        return isActive;
    }

    public static class Builder{
        private String stylistId;
        private String firstName;
        private String lastName;
        private Email email;
        private PhoneNumber phoneNumber;
        private String speciality;
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

        public Builder setEmail(Email email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(PhoneNumber phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }


        public Builder setSpeciality(String speciality) {
            this.speciality = speciality;
            return this;
        }

        public Builder setActive(boolean active) {
            this.isActive = active;
            return this;
        }

        public Stylist build() {
            return new Stylist(this);
        }

    }
}