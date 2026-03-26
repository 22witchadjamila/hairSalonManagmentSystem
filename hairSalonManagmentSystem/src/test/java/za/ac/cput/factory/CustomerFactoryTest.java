package za.ac.cput.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Customer;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFactoryTest {

    @Test
    void createCustomer_success() {
        Customer customer = CustomerFactory.createCustomer(
                "1",
                "Marc",
                "marc@gmail.com",
                "0812345678"
        );

        assertNotNull(customer);
        assertEquals("1", customer.getCustomerId());
        assertEquals("Marc", customer.getName());
        assertEquals("marc@gmail.com", customer.getEmail());
        assertEquals("0812345678", customer.getPhoneNumber());
    }

    // Testing null input intentionally
    @Test
    void createCustomer_nullValues() {
        Customer customer = CustomerFactory.createCustomer(
                null,
                "Marc",
                "marc@gmail.com",
                "0812345678"
        );

        assertNull(customer);
    }

    @Test
    void createCustomer_emptyValues() {
        Customer customer = CustomerFactory.createCustomer(
                "",
                "Marc",
                "marc@gmail.com",
                "0812345678"
        );
        assertNull(customer);
    }
}