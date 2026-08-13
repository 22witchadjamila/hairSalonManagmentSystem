package za.ac.cput.factory;

import za.ac.cput.domain.Role;
import za.ac.cput.util.Helper;

public class RoleFactory {

    public static Role buildRole(String name, String description){
        if(Helper.isNullOrEmpty(name)) return null;

        return new Role.Builder()
                .setRoleId(Helper.generateId())
                .setName(name.trim().toUpperCase())
                .setDescription(description)
                .build();
    }
}
