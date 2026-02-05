package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Teacher assignment to a class
 * For grades 7-12, one teacher can be designated as class teacher/coordinator (isClassTeacher = true)
 */
@Entity
@Table(name = "teacher_assignments", indexes = [
    Index(name = "idx_teacher_assignments_teacher", columnList = "teacher_id"),
    Index(name = "idx_teacher_assignments_class", columnList = "class_id"),
    Index(name = "idx_teacher_assignments_unique", columnList = "teacher_id,class_id", unique = true),
    Index(name = "idx_teacher_assignments_class_teacher", columnList = "class_id,is_class_teacher")
])
class TeacherAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "teacher_id", nullable = false)
    val teacherId: UUID,

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "is_class_teacher", nullable = false)
    val isClassTeacher: Boolean = false,

    @Column(name = "assigned_date", nullable = false)
    val assignedDate: Instant = Instant.now(),

    @Column(name = "assigned_by")
    val assignedBy: UUID? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

