package za.ac.cput.controller;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import za.ac.cput.domain.Role;
import za.ac.cput.service.IRoleService;
import za.ac.cput.service.IUserService;
import za.ac.cput.domain.User;


public class UserController {
    private final IUserService service;
    private final IRoleService roleService;
    public UserController(IUserService service, IRoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }
    public record UserRequest(String username, String email, String firstName,
                              String lastName, String roleId) {}

    @PostMapping
    public User create(@RequestBody UserRequest request) {
        Role role = request.roleId() == null ? null : roleService.read(request.roleId());
        return service.register(request.username(), request.email(),
                request.firstName(), request.lastName(), role);
    }
}
