package za.ac.cput.service;

import za.ac.cput.domain.Customer;

import java.time.LocalDate;

public interface ICustomerService extends IService<Customer, String> {
    Customer register(String firstName, String lastName, String email, String phoneNumber);
    Customer registerWithDetails(String firstname, String lastName, String email,
                                 String phoneNumber, LocalDate dateOfBirth);
    Customer findByEmail(String email);

}
