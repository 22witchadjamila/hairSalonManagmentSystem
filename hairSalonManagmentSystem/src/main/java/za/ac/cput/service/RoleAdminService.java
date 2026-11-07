package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.config.PermissionPolicy;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.User;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.repository.RoleRepository;
import za.ac.cput.repository.UserRepository;
import za.ac.cput.util.Helper;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Custom roles and the permissions they grant. Reached only through /api/roles and /api/users,
 * which are ADMIN-only and not assignable to any custom role - so no role can grant itself
 * more access (no privilege escalation).
 *
 * Built-in roles are fixed: ADMIN has everything; CUSTOMER and STYLIST have their own
 * ownership-checked rules. They cannot be edited, given permissions or deleted.
 *
 * New roles start with NO permissions unless the admin ticks some - least privilege, rather
 * than a convenient default that quietly grants too much.
 */
@Service
public class RoleAdminService {

    private static final Pattern NAME = Pattern.compile("[A-Z][A-Z0-9_]{1,29}");

    private final RoleRepository roles;
    private final UserRepository users;

    public RoleAdminService(RoleRepository roles, UserRepository users) {
        this.roles = roles;
        this.users = users;
    }

    @Transactional
    public Role create(String rawName, String description, Set<String> permissions) {
        String name = rawName == null ? "" : rawName.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
        if (!NAME.matcher(name).matches())
            throw new InvalidOperationException("A role name is 2 to 30 letters, numbers or _, starting with a letter.");
        if (PermissionPolicy.isBuiltIn(name))
            throw new InvalidOperationException(name + " is a built-in role and already exists.");
        if (roles.findByName(name).isPresent())
            throw new InvalidOperationException("A role called " + name + " already exists.");
        return roles.save(new Role.Builder()
                .setRoleId(Helper.generateId())
                .setName(name)
                .setDescription(clean(description))
                .setPermissions(normalise(permissions))
                .build());
    }

    /** Changes take effect on the role's users' next request - permissions are read live. */
    @Transactional
    public Role update(String id, String description, Set<String> permissions) {
        Role existing = custom(id);
        return roles.save(new Role.Builder()
                .setRoleId(existing.getRoleId())
                .setName(existing.getName())
                .setDescription(clean(description))
                .setPermissions(normalise(permissions))
                .build());
    }

    @Transactional
    public void delete(String id) {
        Role role = custom(id);
        int inUse = users.findByRole_Name(role.getName()).size();
        if (inUse > 0)
            throw new InvalidOperationException("The " + role.getName() + " role is still given to " + inUse
                    + (inUse == 1 ? " login" : " logins") + ". Move them to another role first.");
        roles.delete(role);
    }

    /**
     * Moves a staff login to ADMIN or a custom role. Customer and stylist logins are tied to
     * their customer/stylist records and keep their role. Guards against locking the salon out:
     * you can't change your own role, and the last admin can't be moved.
     */
    @Transactional
    public User assignRole(String userId, String roleId, String actingUsername) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.forEntity("User", userId));
        Role target = roles.findById(roleId).orElseThrow(() -> ResourceNotFoundException.forEntity("Role", roleId));
        String current = user.getRole() == null ? null : user.getRole().getName();
        if (user.getUsername().equals(actingUsername))
            throw new InvalidOperationException("You can't change the role of the account you're signed in with.");
        if ("CUSTOMER".equals(current) || "STYLIST".equals(current))
            throw new InvalidOperationException("Customer and stylist logins keep their role - they're linked to their customer or stylist record.");
        if ("CUSTOMER".equals(target.getName()) || "STYLIST".equals(target.getName()))
            throw new InvalidOperationException("Choose ADMIN or a custom role for a staff login.");
        if ("ADMIN".equals(current) && !"ADMIN".equals(target.getName()) && users.findByRole_Name("ADMIN").size() <= 1)
            throw new InvalidOperationException("This is the last admin account, so its role can't be changed.");
        return users.save(new User.Builder()
                .setUserId(user.getUserId())
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setActive(user.isActive())
                .setCreatedAt(user.getCreatedAt())
                .setRole(target)
                .build());
    }

    private Role custom(String id) {
        Role role = roles.findById(id).orElseThrow(() -> ResourceNotFoundException.forEntity("Role", id));
        if (PermissionPolicy.isBuiltIn(role.getName()))
            throw new InvalidOperationException(role.getName() + " is a built-in role and can't be changed or deleted.");
        return role;
    }

    private static Set<String> normalise(Set<String> permissions) {
        try {
            return PermissionPolicy.normalise(permissions);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException(e.getMessage());
        }
    }

    private static String clean(String description) {
        if (description == null || description.isBlank()) return null;
        String d = description.trim();
        if (d.length() > 255) throw new InvalidOperationException("The description can be at most 255 characters.");
        return d;
    }
}
