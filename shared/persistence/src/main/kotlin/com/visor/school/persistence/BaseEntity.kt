package com.visor.school.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Base entity with common fields and auditing
 */
@MappedSuperclass
abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @Column(name = "created_by")
    var createdBy: String? = null

    @Column(name = "updated_by")
    var updatedBy: String? = null

    @Column(name = "version")
    @Version
    var version: Long = 0

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}

