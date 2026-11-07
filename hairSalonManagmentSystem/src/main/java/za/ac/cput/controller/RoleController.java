package za.ac.cput.controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Role;
import za.ac.cput.domain.enums.PermissionAction;
import za.ac.cput.domain.enums.PermissionModule;
import za.ac.cput.service.IRoleService;
import za.ac.cput.service.RoleAdminService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final IRoleService service;
    private final RoleAdminService roleAdmin;

    public RoleController(IRoleService service, RoleAdminService roleAdmin) {
        this.service = service;
        this.roleAdmin = roleAdmin;
    }

    /** permissions: "MODULE:ACTION" strings. Omitted = none (least privilege). */
    public record RoleRequest(String name, String description, Set<String> permissions) {}

    public record ModuleInfo(String key, String label) {}

    /** What can be granted, for the role form's permission grid. Users & Roles is never listed. */
    public record PermissionCatalogue(List<ModuleInfo> modules, List<String> actions) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Role create(@RequestBody RoleRequest request) {
        return roleAdmin.create(request.name(), request.description(), request.permissions());
    }

    @GetMapping("/permissions")
    public PermissionCatalogue permissions() {
        return new PermissionCatalogue(
                Arrays.stream(PermissionModule.values()).map(m -> new ModuleInfo(m.name(), m.getLabel())).toList(),
                Arrays.stream(PermissionAction.values()).map(Enum::name).toList());
    }

    /** Edit a custom role's description and permissions. Built-in roles are refused. */
    @PutMapping("/{id}")
    public Role update(@PathVariable String id, @RequestBody RoleRequest request) {
        return roleAdmin.update(id, request.description(), request.permissions());
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        roleAdmin.delete(id);
    }
}
