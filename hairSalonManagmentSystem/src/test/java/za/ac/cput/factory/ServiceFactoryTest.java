package za.ac.cput.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Service;

import static org.junit.jupiter.api.Assertions.*;
/*ServiceFactoryTest
 Service Factory test class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/
class ServiceFactoryTest {

    @Test
    void createService() {
        Service service = ServiceFactory.createService(
                "001",
                "Haircut",
                150.0,
                30
        );

        assertNotNull(service);
        assertEquals("001", service.getServiceId());
        assertEquals("Haircut", service.getServiceName());
        assertEquals(150.0, service.getPrice());
        assertEquals(30, service.getDuration());
    }
    @Test
    void createService_nullId() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ServiceFactory.createService(null, "Haircut", 150.0, 30);
        });

        assertEquals("Service ID is required", exception.getMessage());
    }
    @Test
    void createService_emptyName() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ServiceFactory.createService("001", "", 150.0, 30);
        });

        assertEquals("Service name is required", exception.getMessage());
    }
    @Test
    void createService_invalidPrice() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ServiceFactory.createService("001", "Haircut", 0, 30);
        });

        assertEquals("Price must be greater than 0", exception.getMessage());
    }
    @Test
    void createService_invalidDuration() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ServiceFactory.createService("001", "Haircut", 150.0, 0);
        });

        assertEquals("Duration must be greater than 0", exception.getMessage());
    }
}