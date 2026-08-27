package za.ac.cput.controller;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Role;
import za.ac.cput.service.IRoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final IRoleService service;

    public RoleController(IRoleService service) {
        this.service = service;
    }

    public record RoleRequest(String name, String description) {}

    @PostMapping
    public Role create(@RequestBody RoleRequest request) {
        return service.register(request.name(), request.description());
    }

    @GetMapping("/{id}")
    public Role read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Role> getAll() {
        return service.getAll();
    }

    @GetMapping("/name/{name}")
    public Role findByName(@PathVariable String name) {
        return service.findByName(name);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
