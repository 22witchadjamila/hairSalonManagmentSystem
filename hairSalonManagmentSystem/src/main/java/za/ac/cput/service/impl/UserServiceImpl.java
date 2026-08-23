package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.UserFactory;
import za.ac.cput.repository.UserRepository;
import za.ac.cput.service.IUserService;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", id));
    }

    @Override
    public User update(User user) {
        read(user.getUserId());
        return repository.save(user);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public User register(String username, String email, String firstName, String lastName, Role role) {
        User user = UserFactory.buildUser(username, email, firstName, lastName, role);
        if (user == null) {
            throw new InvalidOperationException("Invalid user details provided.");
        }
        return create(user);
    }

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

}
