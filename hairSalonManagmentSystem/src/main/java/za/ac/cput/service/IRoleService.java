package za.ac.cput.service;

import za.ac.cput.domain.Role;

public interface IRoleService extends IService<Role, String> {
    Role register(String name, String description);
    Role findByName(String name);
}
