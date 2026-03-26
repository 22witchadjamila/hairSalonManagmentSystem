package za.ac.cput.repository.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import za.ac.cput.domain.Service;

/*ServiceRepositoryTest
 Service Repository test class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/
public class ServiceRepositoryTest {
    private ServiceRepository repository;
    private Service service;
    @BeforeEach
    void setUp() {
        repository = ServiceRepository.getRepository();
        repository.getAll().clear(); // limpa antes de cada teste

        service = new Service.Builder()
                .setServiceId("001")
                .setServiceName("Haircut")
                .setPrice(50)
                .setDuration(30)
                .build();
    }
    @Test
    void testCreate() {
        Service created = repository.create(service);
        assertNotNull(created);
        assertEquals(service.getServiceId(), created.getServiceId());
    }

    @Test
    void testRead() {
        repository.create(service);
        Service read = repository.read("001");
        assertNotNull(read);
        assertEquals("Haircut", read.getServiceName());
    }

    @Test
    void testUpdate() {
        repository.create(service);

        Service updatedService = new Service.Builder()
                .setServiceId("001")
                .setServiceName("Haircut")
                .setPrice(60)
                .setDuration(40)
                .build();

        Service updated = repository.update(updatedService);
        assertNotNull(updated);
        assertEquals(60, updated.getPrice());
        assertEquals(40, updated.getDuration());
    }
    @Test
    void testDelete() {
        repository.create(service);
        boolean deleted = repository.delete("001");
        assertTrue(deleted);
        assertNull(repository.read("001"));
    }
    @Test
    void testGetAll() {
        repository.create(service);
        Set<Service> allServices = repository.getAll();
        assertEquals(1, allServices.size());
        assertTrue(allServices.contains(service));
    }
}
