package com.visor.school.userservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Junction entity linking users to permissions
 */
@Entity
@Table(name = "user_permissions", indexes = [
    Index(name = "idx_user_permissions_user_id", columnList = "user_id"),
    Index(name = "idx_user_permissions_permission_id", columnList = "permission_id"),
    Index(name = "idx_user_permissions_unique", columnList = "user_id,permission_id", unique = true)
])
class UserPermission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    val permission: Permission,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: Instant = Instant.now(),

    @Column(name = "assigned_by")
    val assignedBy: UUID? = null
)

