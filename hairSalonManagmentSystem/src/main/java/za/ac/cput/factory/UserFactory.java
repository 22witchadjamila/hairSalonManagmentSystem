package za.ac.cput.factory;

import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.util.Helper;

import java.time.LocalDateTime;

public class UserFactory {

    public static User buildUser(String username, String email,
                                 String firstName, String lastName, Role role) {
        if (Helper.isNullOrEmpty(username)) return null;
        if (!Helper.isValidEmail(email)) return null;
        if (Helper.isNullOrEmpty(firstName)) return null;
        if (Helper.isNullOrEmpty(lastName)) return null;

        return new User.Builder()
                .setUserId(Helper.generateId())
                .setUsername(username.trim())
                .setEmail(Email.of(email))
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setActive(true)
                .setCreatedAt(LocalDateTime.now())
                .setRole(role)
                .build();
    }
}
