package za.ac.cput.factory;

import za.ac.cput.domain.Customer;

public class CustomerFactory {
    public static Customer createCustomer(String id, String name, String email, String phoneNumber) {

        if(id == null || name == null || email == null || phoneNumber == null) {
            return null;
        }

        return new Customer.Builder()
                .setCustomerId(id)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .build();
    }
}
