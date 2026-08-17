package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.User;
import za.ac.cput.repository.UserRepository;
import za.ac.cput.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user){
        return repository.save(user);
    }


}
