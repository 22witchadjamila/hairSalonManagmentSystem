package za.ac.cput.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.User;
import za.ac.cput.config.SessionControl;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.RoleAdminService;
import za.ac.cput.service.IUserService;

import java.util.List;

/**
 * Login administration. Admin only - enforced by SecurityConfig and the annotation below.
 *
 * Creating logins: customers sign themselves up, stylist logins are created from the Stylists
 * page (they must be attached to a Stylist record), so this endpoint creates ADMIN logins only.
 * It used to accept a username with no password, which produced accounts that could never sign
 * in; every login now goes through AuthService with a hashed password.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final IUserService service;
    private final AuthService authService;
    private final RoleAdminService roleAdmin;
    private final SessionControl sessionControl;

    public UserController(IUserService service, AuthService authService, RoleAdminService roleAdmin,
                          SessionControl sessionControl) {
        this.service = service;
        this.authService = authService;
        this.roleAdmin = roleAdmin;
        this.sessionControl = sessionControl;
    }

    /** roleId: null/blank = ADMIN (the original behaviour), otherwise a custom role. */
    public record AdminUserRequest(@NotBlank String username, @NotBlank String password,
                                   @NotBlank @Email String email,
                                   @NotBlank String firstName, @NotBlank String lastName,
                                   String roleId) {}

    public record RoleChange(@NotBlank String roleId) {}

    public record PasswordReset(@NotBlank String newPassword) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody AdminUserRequest request) {
        if (request.roleId() == null || request.roleId().isBlank())
            return authService.createAdmin(request.username(), request.password(), request.email(),
                    request.firstName(), request.lastName());
        return authService.createStaff(request.username(), request.password(), request.email(),
                request.firstName(), request.lastName(), request.roleId());
    }

    /**
     * Set another person's password. Their open sessions end, so whoever had the old password (or a
     * stolen session) is signed out. Recorded in the audit log by AuditInterceptor.
     */
    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable String id, @Valid @RequestBody PasswordReset request,
                              Authentication authentication) {
        String username = authService.resetPassword(id, request.newPassword(), authentication.getName());
        sessionControl.expireSessionsOf(username, null);
    }

    /** Move a staff login to ADMIN or a custom role. Their open session ends (SessionFreshnessFilter). */
    @PutMapping("/{id}/role")
    public User changeRole(@PathVariable String id, @Valid @RequestBody RoleChange request,
                           Authentication authentication) {
        return roleAdmin.assignRole(id, request.roleId(), authentication.getName());
    }

    @GetMapping("/{id}")
    public User read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/username/{username}")
    public User findByUsername(@PathVariable String username) {
        return service.findByUsername(username);
    }

    /** 204 on success. Refuses to delete yourself or the last admin - see AuthService.deleteLogin. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, Authentication authentication) {
        authService.deleteLogin(id, authentication.getName());
    }
}
