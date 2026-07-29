/* CustomerFactory.java
Factory class responsible for creating validated Customer objects using the Builder pattern.
Author: Marc Kabala (230701876)
Date: 20 March 2026
*/

package za.ac.cput.factory;

import za.ac.cput.domain.Customer;
import za.ac.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class CustomerFactory {

    public static Customer buildCustomer(String firstName, String lastName,
                                         String email, String phoneNumber) {
        if(firstName == null || firstName.isBlank()) return null;
        if(lastName == null || lastName.isBlank()) return null;
        if(email == null || !email.contains("@")) return null;
        if(phoneNumber == null || phoneNumber.isBlank()) return null;

        return new Customer.Builder()
                .setCustomerId(UUID.randomUUID().toString())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(email.trim().toLowerCase())
                .setPhoneNumber(phoneNumber.trim())
                .setRegisteredAt(LocalDateTime.now())
                .build();
    }

    public static Customer buildCustomerWithDetails(String firstName, String lastName,
                                                    String email, String phoneNumber,
                                                    LocalDate dateOfBirth){
        if(firstName == null || firstName.isBlank()) return null;
        if(lastName == null || lastName.isBlank()) return null;
        if(email == null || !email.contains("@")) return null;
        if(phoneNumber == null || phoneNumber.isBlank()) return null;

        return new Customer.Builder()
                .setCustomerId(UUID.randomUUID().toString())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(email.trim().toLowerCase())
                .setPhoneNumber(phoneNumber.trim())
                .setDateOfBirth(dateOfBirth)
                .setRegisteredAt(LocalDateTime.now())
                .build();
    }
}
