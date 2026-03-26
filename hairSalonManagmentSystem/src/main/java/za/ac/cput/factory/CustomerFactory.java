/* CustomerFactory.java
Factory class responsible for creating validated Customer objects using the Builder pattern.
Author: Marc Kabala (230701876)
Date: 20 March 2026
*/

package za.ac.cput.factory;

import za.ac.cput.domain.Customer;

public class CustomerFactory {
    public static Customer createCustomer(String id, String name, String email, String phoneNumber) {

        // simple null or empty check
        if (id == null || id.isEmpty() ||
            name == null || name.isEmpty() ||
            email == null || email.isEmpty() ||
            phoneNumber == null || phoneNumber.isEmpty()) {
            return null; // still returning null if invalid
        }

        return new Customer.Builder()
                .setCustomerId(id)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(Integer.parseInt(phoneNumber))
                .build();
    }
}
