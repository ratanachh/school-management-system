package com.visor.school.userservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * User entity representing a person with system access
 * Password management is delegated to Keycloak via keycloakId reference
 * Users can have multiple roles (many-to-many relationship)
 */
@Entity
@Table(name = "users", indexes = [
    Index(name = "idx_users_keycloak_id", columnList = "keycloak_id", unique = true),
    Index(name = "idx_users_email", columnList = "email", unique = true)
])
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "keycloak_id", nullable = false, unique = true)
    val keycloakId: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var roles: MutableSet<UserRole> = mutableSetOf(),

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    var accountStatus: AccountStatus = AccountStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L,

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null
) {
    /**
     * Check if user has a specific role
     */
    fun hasRole(role: UserRole): Boolean = roles.contains(role)

    /**
     * Check if user has any of the specified roles
     */
    fun hasAnyRole(vararg roles: UserRole): Boolean = roles.any { this.roles.contains(it) }

    /**
     * Add a role to the user
     */
    fun addRole(role: UserRole) {
        roles.add(role)
        updatedAt = Instant.now()
    }

    /**
     * Remove a role from the user
     */
    fun removeRole(role: UserRole) {
        roles.remove(role)
        updatedAt = Instant.now()
    }

    /**
     * Replace all roles with new roles (replaces all existing roles)
     */
    fun replaceRoles(newRoles: Set<UserRole>) {
        roles.clear()
        roles.addAll(newRoles)
        updatedAt = Instant.now()
    }

    fun updateLastLogin() {
        lastLoginAt = Instant.now()
        updatedAt = Instant.now()
    }

    fun verifyEmail() {
        emailVerified = true
        updatedAt = Instant.now()
    }

    fun updateStatus(status: AccountStatus) {
        accountStatus = status
        updatedAt = Instant.now()
    }
}

