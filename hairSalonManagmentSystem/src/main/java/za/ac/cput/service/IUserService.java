package za.ac.cput.service;

import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;

public interface IUserService extends IService<User, String> {
    User register(String username, String email, String firstName, String lastName, Role role);
    User findByUsername(String username);
}
