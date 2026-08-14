package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Stylist;

import java.util.List;

public interface StylistRepository extends JpaRepository<Stylist, String> {
    List<Stylist> findByIsActiveTrue();
}
