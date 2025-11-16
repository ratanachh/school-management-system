package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Teacher entity
 */
@Entity
@Table(name = "teachers", indexes = [
    Index(name = "idx_teachers_employee_id", columnList = "employee_id", unique = true),
    Index(name = "idx_teachers_user_id", columnList = "user_id"),
    Index(name = "idx_teachers_employment_status", columnList = "employment_status")
])
class Teacher(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "employee_id", nullable = false, unique = true)
    val employeeId: String,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @ElementCollection
    @CollectionTable(name = "teacher_qualifications", joinColumns = [JoinColumn(name = "teacher_id")])
    @Column(name = "qualification")
    val qualifications: List<String> = emptyList(),

    @ElementCollection
    @CollectionTable(name = "teacher_subject_specializations", joinColumns = [JoinColumn(name = "teacher_id")])
    @Column(name = "subject")
    val subjectSpecializations: List<String> = emptyList(),

    @Column(name = "hire_date", nullable = false)
    val hireDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    var employmentStatus: EmploymentStatus = EmploymentStatus.ACTIVE,

    @Column(name = "department")
    val department: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L
) {
    init {
        require(subjectSpecializations.isNotEmpty()) {
            "Teacher must have at least one subject specialization"
        }
    }

    fun updateEmploymentStatus(status: EmploymentStatus) {
        employmentStatus = status
        updatedAt = Instant.now()
    }
}

