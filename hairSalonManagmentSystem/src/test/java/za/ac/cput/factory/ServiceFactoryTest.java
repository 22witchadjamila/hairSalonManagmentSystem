package za.ac.cput.factory;

/*ServiceFactoryTest
 Service Factory test class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Service;
import za.ac.cput.domain.enums.ServiceCategory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceFactory Tests")
class ServiceFactoryTest {

    @Test
    @DisplayName("Should create a valid active service")
    void shouldCreateValidService() {
        Service service = ServiceFactory.buildService(
                "Blowout", "Full blowout and style",
                60, new BigDecimal("200.00"), ServiceCategory.STYLING);
        assertNotNull(service);
        assertEquals("Blowout", service.getName());
        assertEquals(60, service.getDurationMinutes());
        assertEquals(0, new BigDecimal("200.00").compareTo(service.getPrice()));
        assertEquals(ServiceCategory.STYLING, service.getCategory());
        assertTrue(service.isActive());
    }

    @Test
    @DisplayName("Should return null when name is blank")
    void shouldReturnNullWhenNameIsBlank() {
        Service service = ServiceFactory.buildService(
                "", "desc", 60, new BigDecimal("200"), ServiceCategory.STYLING);
        assertNull(service);
    }

    @Test
    @DisplayName("Should return null when duration is zero")
    void shouldReturnNullWhenDurationIsZero() {
        Service service = ServiceFactory.buildService(
                "Cut", "desc", 0, new BigDecimal("100"), ServiceCategory.HAIRCUT);
        assertNull(service);
    }

    @Test
    @DisplayName("Should return null when price is zero")
    void shouldReturnNullWhenPriceIsZero() {
        Service service = ServiceFactory.buildService(
                "Cut", "desc", 30, BigDecimal.ZERO, ServiceCategory.HAIRCUT);
        assertNull(service);
    }

    @Test
    @DisplayName("Should return null when price is negative")
    void shouldReturnNullWhenPriceIsNegative() {
        Service service = ServiceFactory.buildService(
                "Cut", "desc", 30, new BigDecimal("-50"), ServiceCategory.HAIRCUT);
        assertNull(service);
    }

    @Test
    @DisplayName("Should return null when category is null")
    void shouldReturnNullWhenCategoryIsNull() {
        Service service = ServiceFactory.buildService(
                "Cut", "desc", 30, new BigDecimal("100"), null);
        assertNull(service);
    }
}