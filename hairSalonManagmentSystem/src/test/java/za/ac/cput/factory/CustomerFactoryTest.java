/* CustomerFactoryTest.java
Junit Test
Author: Marc Kabala (230701876)
Date: 20 March 2026
*/

package za.ac.cput.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Customer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerFactory Test")
public class CustomerFactoryTest {

    @Test
    @DisplayName("Customer Factory Test")
    void shouldCreateValidCustomer(){
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", "0821234567");
        assertNotNull(customer);
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("jane@email.com", customer.getEmail().getValue());
        assertEquals("0821234567", customer.getPhoneNumber().getValue());
        assertNotNull(customer.getCustomerId());
        assertNotNull(customer.getRegisteredAt());
    }

    @Test
    @DisplayName("Should return null when first name is blank")
    void shouldReturnNullWhenFirstNameIsBlank(){
        Customer customer = CustomerFactory.buildCustomer(
                "", "Doe", "jane@email.com", "082");
        assertNull(customer);
    }

    @Test
    @DisplayName("Should return null when email has no @ symbol")
    void shouldReturnNullWhenEmailIsInvalid(){
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "invalidemail", "082");
        assertNull(customer);
    }

    @Test
    @DisplayName("Should return null when phone number is null")
    void shouldReturnNullWhenPhoneNumberIsNull(){
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", null);
        assertNull(customer);
    }

    @Test
    @DisplayName("Should normalise email to lowercase")
    void shouldNormaliseEmailToLowerCase(){
        Customer customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "JANE@EMAIL.COM", "082");
        assertNotNull(customer);
        assertEquals("jane@email.com", customer.getEmail().getValue());
    }

    @Test
    @DisplayName("Should create customer with full details")
    void shouldCreateCustomerWithAllDetails(){
        LocalDate dob = LocalDate.of(2026, 6, 15);
        Customer customer = CustomerFactory.buildCustomerWithDetails(
                "Jane", "Doe", "jane@email.com", "082", dob);
        assertNotNull(customer);
        assertEquals(dob, customer.getDateOfBirth());
    }

    @Test
    @DisplayName("Should generate a unique ID for each customer")
    void shouldGenerateUniqueIds(){
        Customer c1 = CustomerFactory.buildCustomer(
                "A", "B", "a@b.com", "001");
        Customer c2 = CustomerFactory.buildCustomer("C", "D",
                "c@d.com", "002");
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotEquals(c1.getCustomerId(), c2.getCustomerId());
    }
}