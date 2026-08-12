package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.valueobject.Email;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String userId;
    private String username;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    protected User() {}

    public User(Builder builder) {
        this.userId    = builder.userId;
        this.username  = builder.username;
        this.email     = builder.email;
        this.firstName = builder.firstName;
        this.lastName  = builder.lastName;
        this.isActive  = builder.isActive;
        this.createdAt = builder.createdAt;
        this.role      = builder.role;
    }

    public String getUserId()          { return userId; }
    public String getUsername()        { return username; }
    public Email getEmail()            { return email; }
    public String getFirstName()       { return firstName; }
    public String getLastName()        { return lastName; }
    public boolean isActive()          { return isActive; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public Role getRole()              { return role; }

    public static class Builder {
        private String userId;
        private String username;
        private Email email;
        private String firstName;
        private String lastName;
        private boolean isActive;
        private LocalDateTime createdAt;
        private Role role;

        public Builder setUserId(String userId) {
            this.userId = userId; return this;
        }
        public Builder setUsername(String username) {
            this.username = username; return this;
        }
        public Builder setEmail(Email email) {
            this.email = email; return this;
        }
        public Builder setFirstName(String firstName) {
            this.firstName = firstName; return this;
        }
        public Builder setLastName(String lastName) {
            this.lastName = lastName; return this;
        }
        public Builder setActive(boolean isActive) {
            this.isActive = isActive; return this;
        }
        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt; return this;
        }
        public Builder setRole(Role role) {
            this.role = role; return this;
        }

        public User build() {
            return new User(this);
        }
    }

}
