package za.ac.cput.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    private String roleId;
    private String name;
    private String description;

    protected Role() {}

    public Role(Builder builder) {
        this.roleId      = builder.roleId;
        this.name        = builder.name;
        this.description = builder.description;
    }

    public String getRoleId()      { return roleId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }

    public static class Builder {
        private String roleId;
        private String name;
        private String description;

        public Builder setRoleId(String roleId) {
            this.roleId = roleId; return this;
        }
        public Builder setName(String name) {
            this.name = name; return this;
        }
        public Builder setDescription(String description) {
            this.description = description; return this;
        }
        public Role build() {
            return new Role(this);
        }
    }
}
