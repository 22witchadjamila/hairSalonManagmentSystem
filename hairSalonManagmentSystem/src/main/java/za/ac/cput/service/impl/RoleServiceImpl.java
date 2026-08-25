package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Role;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.RoleFactory;
import za.ac.cput.repository.RoleRepository;
import za.ac.cput.service.IRoleService;

import java.util.List;

@Service
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository repository;

    public RoleServiceImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role create(Role role) {
        return repository.save(role);
    }

    @Override
    public Role read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Role", id));
    }

    @Override
    public Role update(Role role) {
        read(role.getRoleId());
        return repository.save(role);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Role> getAll() {
        return repository.findAll();
    }

    @Override
    public Role register(String name, String description) {
        Role role = RoleFactory.buildRole(name, description);
        if (role == null) {
            throw new InvalidOperationException("Invalid role details provided.");
        }
        return create(role);
    }

    @Override
    public Role findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }
}
