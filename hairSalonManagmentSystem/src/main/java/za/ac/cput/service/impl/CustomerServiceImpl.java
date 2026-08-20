package za.ac.cput.service.impl;

import za.ac.cput.domain.Customer;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.CustomerFactory;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.service.ICustomerService;

import java.time.LocalDate;
import java.util.List;

public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer create(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public Customer read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", id));
    }

    @Override
    public Customer update(Customer customer) {
        read(customer.getCustomerId());
        return repository.save(customer);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Customer> getAll(){
        return repository.findAll();
    }

    @Override
    public Customer register(String firstName, String lastName, String email, String phoneNumber) {
        Customer customer = CustomerFactory.buildCustomer(firstName, lastName, email, phoneNumber);
        if (customer == null){
            throw new InvalidOperationException("Invalid customer details provided.");
        }
        return create(customer);
    }

    @Override
    public Customer registerWithDetails(String firstName, String lastName, String email,
                                        String phoneNumber, LocalDate dateOfBirth) {
        Customer customer = CustomerFactory.buildCustomerWithDetails(
                firstName, lastName, email, phoneNumber, dateOfBirth);
        if (customer == null){
            throw new InvalidOperationException("Invalid customer details provided.");
        }
        return create(customer);
    }

    @Override
    public Customer findByEmail(String email) {
        return repository.findByEmail_Value(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email." + email));
    }

}
