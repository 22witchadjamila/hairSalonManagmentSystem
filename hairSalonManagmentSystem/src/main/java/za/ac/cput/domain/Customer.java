/* Customer.java
- Domain model class representing a Customer entity with attributes and
Builder pattern for object creation.
 Author: Marc Kabala
 Date: 20 March 2026
 */

package za.ac.cput.domain;

import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private String customerId;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private LocalDateTime registeredAt;

    protected Customer(){}

    public Customer(Builder builder){
        this.customerId = builder.customerId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.dateOfBirth = builder.dateOfBirth;
        this.registeredAt = builder.registeredAt;
    }

    public String getCustomerId() {
        return customerId;
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
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public static class Builder {
        private String customerId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private LocalDateTime registeredAt;

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
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

        public Builder setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder setRegisteredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
