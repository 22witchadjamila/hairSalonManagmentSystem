package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;

import java.util.List;

public interface SalonServiceRepository extends JpaRepository<SalonService, String> {
    List<SalonService> findByCategory(ServiceCategory category);
    List<SalonService> findByIsActiveTrue();
}
