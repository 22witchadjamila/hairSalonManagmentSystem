package za.ac.cput.domain;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    private String roleId;
    private String name;
    private String description;

    /**
     * "MODULE:ACTION" strings, e.g. "APPOINTMENTS:VIEW" - see PermissionModule/PermissionAction.
     * Only custom roles use these. ADMIN always has everything; CUSTOMER and STYLIST have their
     * own fixed, ownership-checked rules in SecurityConfig. Loaded eagerly: they are read on every
     * request by PermissionPolicy, always together with the role.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 40)
    private Set<String> permissions = new HashSet<>();

    protected Role() {}

    public Role(Builder builder) {
        this.roleId      = builder.roleId;
        this.name        = builder.name;
        this.description = builder.description;
        this.permissions = new HashSet<>(builder.permissions);
    }

    public String getRoleId()      { return roleId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Set<String> getPermissions() { return Collections.unmodifiableSet(permissions); }

    public static class Builder {
        private String roleId;
        private String name;
        private String description;
        private Set<String> permissions = new HashSet<>();

        public Builder setRoleId(String roleId) {
            this.roleId = roleId; return this;
        }
        public Builder setName(String name) {
            this.name = name; return this;
        }
        public Builder setDescription(String description) {
            this.description = description; return this;
        }
        public Builder setPermissions(Set<String> permissions) {
            this.permissions = permissions == null ? new HashSet<>() : new HashSet<>(permissions); return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}
