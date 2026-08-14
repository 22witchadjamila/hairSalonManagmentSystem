package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Promotion;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);
}
