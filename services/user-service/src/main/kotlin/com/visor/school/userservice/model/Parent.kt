package com.visor.school.userservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Parent entity linking users to their children (students)
 */
@Entity
@Table(name = "parents", indexes = [
    Index(name = "idx_parents_user_id", columnList = "user_id"),
    Index(name = "idx_parents_student_id", columnList = "student_id"),
    Index(name = "idx_parents_unique", columnList = "user_id,student_id", unique = true)
])
class Parent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "student_id", nullable = false)
    val studentId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false)
    val relationship: Relationship,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

