package service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Customer;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.CustomerFactory;
import za.ac.cput.repository.CustomerRepository;
import za.ac.cput.service.impl.CustomerServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Mockito unit tests for CustomerServiceImpl.
 * The repository is mocked, so these tests exercise the service's own logic
 * (validation via the factory, exception translation) without touching a real database
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl Tests")
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = CustomerFactory.buildCustomer("Jane", "Dan", "jane@gmail.com", "0821234567");
    }

    @Test
    @DisplayName("create() saves and returns the customer")
    void create_savesAndReturnsCustomer() {
        when(repository.save(sampleCustomer)).thenReturn(sampleCustomer);

        Customer result = service.create(sampleCustomer);

        assertEquals(sampleCustomer, result);
        verify(repository).save(sampleCustomer);
    }

    @Test
    @DisplayName("read() returns the customer when found")
    void read_returnsCustomer_whenFound() {
        when(repository.findById(sampleCustomer.getCustomerId())).thenReturn(Optional.of(sampleCustomer));

        Customer result = service.read(sampleCustomer.getCustomerId());

        assertEquals(sampleCustomer, result);
    }

    @Test
    @DisplayName("read() throws ResourceNotFoundException when the id doesn't exist")
    void read_throws_whenNotFound(){
        when(repository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.read("missing-id"));
    }

    @Test
    @DisplayName("update() checks the customer exists, then saves")
    void update_savesWhenExists(){

        when(repository.findById(sampleCustomer.getCustomerId())).thenReturn(Optional.of(sampleCustomer));
        when(repository.save(sampleCustomer)).thenReturn(sampleCustomer);

        Customer result = service.update(sampleCustomer);
        assertEquals(sampleCustomer, result);
        verify(repository).save(sampleCustomer);
    }

    @Test
    @DisplayName("update() throws when the customer being updated doesn't exist")
    void update_throws_whenNotFound(){
        when(repository.findById(sampleCustomer.getCustomerId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(sampleCustomer));
        verify(repository, never()).save(any());
    }
}
