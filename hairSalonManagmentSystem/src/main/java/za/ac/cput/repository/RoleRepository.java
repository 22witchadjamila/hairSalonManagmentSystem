package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
