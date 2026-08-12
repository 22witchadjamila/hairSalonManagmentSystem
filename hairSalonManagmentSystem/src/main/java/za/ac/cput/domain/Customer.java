/* Customer.java
- Domain model class representing a Customer entity with attributes and
Builder pattern for object creation.
 Author: Marc Kabala
 Date: 20 March 2026
 */

package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;

import java.time.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private String customerId;

    private String firstName;
    private String lastName;

    /**
     *The @AttributeOverride annotation renames a database column from an embedded object. In your code,
     * it changes the column name for the value field inside the Email class to email in the main table.
     * This stops column name clashes when you embed the same object more than once.
     * */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number"))
    private PhoneNumber phoneNumber;

    private LocalDate dateOfBirth;
    private LocalDateTime registeredAt;

    protected Customer(){}

    public Customer(Builder builder){
        this.customerId = builder.customerId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
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
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    public Email getEmail() {return email;}
    public PhoneNumber getPhoneNumber() {return phoneNumber;}

    public static class Builder {
        private String customerId;
        private String firstName;
        private String lastName;
        private Email email;
        private PhoneNumber phoneNumber;
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

        public Builder setEmail(Email email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(PhoneNumber phoneNumber) {
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
