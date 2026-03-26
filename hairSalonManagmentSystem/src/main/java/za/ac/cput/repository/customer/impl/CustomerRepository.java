/* CustomerRepository.java
- Repository implementation handling CRUD operations for Customer objects as the data access layer.
Author: Marc Kabala
Date: 26 March 2026
*/

package za.ac.cput.repository.customer.impl;

import za.ac.cput.domain.Customer;
import za.ac.cput.repository.customer.ICustomerRepository;

import java.util.HashSet;
import java.util.Set;

public class CustomerRepository implements ICustomerRepository {

    private static CustomerRepository repository = null;
    private Set<Customer> customerDB;

    private CustomerRepository() {
        customerDB = new HashSet<>();
    }

    public static CustomerRepository getRepository() {
        if (repository == null) {
            repository = new CustomerRepository();
        }
        return repository;
    }

    @Override
    public Customer create(Customer customer) {
        customerDB.add(customer);
        return customer;
    }

    @Override
    public Customer read(String id) {
        return customerDB.stream()
                .filter(c -> c.getCustomerId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Customer update(Customer customer) {
        Customer old = read(customer.getCustomerId());
        if (old != null) {
            customerDB.remove(old);
            customerDB.add(customer);
            return customer;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Customer customer = read(id);
        if (customer != null) {
            customerDB.remove(customer);
            return true;
        }
        return false;
    }

    @Override
    public Set<Customer> getAll() {
        return customerDB;
    }
}
