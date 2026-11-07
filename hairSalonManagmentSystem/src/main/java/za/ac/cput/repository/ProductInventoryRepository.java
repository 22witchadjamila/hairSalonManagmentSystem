package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.ProductInventory;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {

    /** Products used by a service also block that service from being hard-deleted. */
    boolean existsBySalonService_ServiceId(String serviceId);

    /** Retail products customers may see - see ProductInventory.forSale. */
    java.util.List<ProductInventory> findByForSaleTrue();
}
