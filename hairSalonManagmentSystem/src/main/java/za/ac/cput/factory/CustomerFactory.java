/* CustomerFactory.java
Factory class responsible for creating validated Customer objects using the Builder pattern.
Author: Marc Kabala (230701876)
Date: 20 March 2026
*/

package za.ac.cput.factory;

import za.ac.cput.domain.Customer;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;
import za.ac.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerFactory {

    public static Customer buildCustomer(String firstName, String lastName,
                                         String email, String phoneNumber) {
        if (Helper.isNullOrEmpty(firstName)) return null;
        if (Helper.isNullOrEmpty(lastName)) return null;
        if (!Helper.isValidEmail(email)) return null;
        if (Helper.isNullOrEmpty(phoneNumber)) return null;

        return new Customer.Builder()
                .setCustomerId(Helper.generateId())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(Email.of(email))
                .setPhoneNumber(PhoneNumber.of(phoneNumber))
                .setRegisteredAt(LocalDateTime.now())
                .build();
    }

    public static Customer buildCustomerWithDetails(String firstName, String lastName,
                                                    String email, String phoneNumber,
                                                    LocalDate dateOfBirth){
        if (Helper.isNullOrEmpty(firstName)) return null;
        if (Helper.isNullOrEmpty(lastName)) return null;
        if (!Helper.isValidEmail(email)) return null;
        if (Helper.isNullOrEmpty(phoneNumber)) return null;

        return new Customer.Builder()
                .setCustomerId(Helper.generateId())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(Email.of(email))
                .setPhoneNumber(PhoneNumber.of(phoneNumber))
                .setDateOfBirth(dateOfBirth)
                .setRegisteredAt(LocalDateTime.now())
                .build();
    }
}
