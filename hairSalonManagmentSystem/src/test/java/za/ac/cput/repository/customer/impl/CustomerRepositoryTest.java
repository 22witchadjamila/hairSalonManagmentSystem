package za.ac.cput.repository.customer.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Customer;
import za.ac.cput.factory.CustomerFactory;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest {

    private static CustomerRepository repository;
    private static Customer customer;

    @BeforeAll
    static void setup(){
        repository = CustomerRepository.getRepository();
        customer = CustomerFactory.createCustomer("1", "Marc", "marc01@gmail.com", "0684527413");
    }

    @Test
    void create() {
        Customer created = repository.create(customer);
        assertNotNull(created);
        System.out.println("Created: " + created);
    }

    @Test
    void read() {
        Customer read = repository.read(customer.getCustomerId());
        assertNotNull(read);
        System.out.println("Read: " + read);
    }

    @Test
    void update() {
        Customer updated = new Customer.Builder()
                .setCustomerId(customer.getCustomerId())
                .setName("Updated Name")
                .setPhoneNumber("0987654321")
                .build();

        Customer result = repository.update(updated);
        assertNotNull(result);
        System.out.println("Updated: " + result);
    }

    @Test
    void delete() {
        boolean success = repository.delete(customer.getCustomerId());
        assertTrue(success);
        System.out.println("Deleted: " + success);
    }

    @Test
    void getAll() {
        assertFalse(repository.getAll().isEmpty());
        System.out.println("All Customers: " + repository.getAll());
    }
}