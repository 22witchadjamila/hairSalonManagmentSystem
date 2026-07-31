package za.ac.cput.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.ProductInventory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductInventoryFactory Tests")
class ProductInventoryFactoryTest {

    @Test
    @DisplayName("Should create a valid product")
    void shouldCreateValidProduct() {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                "Argan Oil Shampoo", "OGX", "Hair Care",
                50, 10,
                new BigDecimal("80.00"), new BigDecimal("150.00"));
        assertNotNull(product);
        assertEquals("Argan Oil Shampoo", product.getName());
        assertEquals("OGX", product.getBrand());
        assertEquals(50, product.getStockQuantity());
        assertEquals(10, product.getReorderLevel());
        assertNotNull(product.getProductId());
    }

    @Test
    @DisplayName("Should return null when name is blank")
    void shouldReturnNullWhenNameIsBlank() {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                "", "OGX", "Hair Care", 50, 10,
                new BigDecimal("80"), new BigDecimal("150"));
        assertNull(product);
    }

    @Test
    @DisplayName("Should return null when stock quantity is negative")
    void shouldReturnNullWhenStockIsNegative() {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                "Shampoo", "OGX", "Hair Care", -1, 10,
                new BigDecimal("80"), new BigDecimal("150"));
        assertNull(product);
    }

    @Test
    @DisplayName("Should return null when selling price is zero")
    void shouldReturnNullWhenSellingPriceIsZero() {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                "Shampoo", "OGX", "Hair Care", 20, 5,
                new BigDecimal("80"), BigDecimal.ZERO);
        assertNull(product);
    }

    @Test
    @DisplayName("Should return null when cost price is null")
    void shouldReturnNullWhenCostPriceIsNull() {
        ProductInventory product = ProductInventoryFactory.buildProduct(
                "Shampoo", "OGX", "Hair Care", 20, 5,
                null, new BigDecimal("150"));
        assertNull(product);
    }
}
