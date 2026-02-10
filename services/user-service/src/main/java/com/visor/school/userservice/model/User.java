package com.visor.school.userservice.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * User entity representing a person with system access
 * Password management is delegated to Keycloak via keycloakId reference
 * Users can have multiple roles (many-to-many relationship)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_keycloak_id", columnList = "keycloak_id", unique = true),
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0L;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected User() {}

    public User(String keycloakId, String email, String firstName, String lastName, Set<UserRole> roles, String phoneNumber, boolean emailVerified, AccountStatus accountStatus) {
        this.keycloakId = keycloakId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        if (roles != null) {
            this.roles.addAll(roles);
        }
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        if (accountStatus != null) {
            this.accountStatus = accountStatus;
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    // Domain methods

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(UserRole... roles) {
        return Stream.of(roles).anyMatch(this.roles::contains);
    }

    /**
     * Add a role to the user
     */
    public void addRole(UserRole role) {
        roles.add(role);
        updatedAt = Instant.now();
    }

    /**
     * Remove a role from the user
     */
    public void removeRole(UserRole role) {
        roles.remove(role);
        updatedAt = Instant.now();
    }

    /**
     * Replace all roles with new roles (replaces all existing roles)
     */
    public void replaceRoles(Set<UserRole> newRoles) {
        roles.clear();
        roles.addAll(newRoles);
        updatedAt = Instant.now();
    }

    public void updateLastLogin() {
        lastLoginAt = Instant.now();
        updatedAt = Instant.now();
    }

    public void verifyEmail() {
        emailVerified = true;
        updatedAt = Instant.now();
    }

    public void updateStatus(AccountStatus status) {
        accountStatus = status;
        updatedAt = Instant.now();
    }
}
