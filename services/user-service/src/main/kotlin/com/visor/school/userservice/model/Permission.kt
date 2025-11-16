package com.visor.school.userservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Permission entity for fine-grained access control
 * Permissions are synced with Keycloak and included in JWT tokens
 */
@Entity
@Table(name = "permissions", indexes = [
    Index(name = "idx_permissions_key", columnList = "permission_key", unique = true)
])
class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "permission_key", nullable = false, unique = true)
    val permissionKey: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    val category: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun updateDescription(description: String) {
        this.description = description
        this.updatedAt = Instant.now()
    }
}

