package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.ProductInventory;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {
}
