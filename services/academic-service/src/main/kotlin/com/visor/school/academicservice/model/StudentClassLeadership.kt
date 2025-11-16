package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Student class leadership assignment
 * Links students to their leadership positions in classes
 */
@Entity
@Table(name = "student_class_leadership", indexes = [
    Index(name = "idx_leadership_student", columnList = "student_id"),
    Index(name = "idx_leadership_class", columnList = "class_id"),
    Index(name = "idx_leadership_class_position", columnList = "class_id,leadership_position", unique = true),
    Index(name = "idx_leadership_student_class", columnList = "student_id,class_id", unique = true)
])
class StudentClassLeadership(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "student_id", nullable = false)
    val studentId: UUID,

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "leadership_position", nullable = false)
    val leadershipPosition: LeadershipPosition,

    @Column(name = "assigned_by", nullable = false)
    val assignedBy: UUID,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

