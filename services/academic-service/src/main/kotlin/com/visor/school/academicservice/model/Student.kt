package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Student entity for K12 school management (grades 1-12)
 */
@Entity
@Table(name = "students", indexes = [
    Index(name = "idx_students_student_id", columnList = "student_id", unique = true),
    Index(name = "idx_students_user_id", columnList = "user_id"),
    Index(name = "idx_students_grade_level", columnList = "grade_level"),
    Index(name = "idx_students_enrollment_status", columnList = "enrollment_status")
])
class Student(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "student_id", nullable = false, unique = true)
    val studentId: String,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "date_of_birth", nullable = false)
    val dateOfBirth: LocalDate,

    @Column(name = "grade_level", nullable = false)
    var gradeLevel: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false)
    var enrollmentStatus: EnrollmentStatus = EnrollmentStatus.ENROLLED,

    @Embedded
    val address: Address? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "name", column = Column(name = "emergency_contact_name")),
        AttributeOverride(name = "relationship", column = Column(name = "emergency_contact_relationship")),
        AttributeOverride(name = "phoneNumber", column = Column(name = "emergency_contact_phone")),
        AttributeOverride(name = "email", column = Column(name = "emergency_contact_email")),
        AttributeOverride(name = "address", column = Column(name = "emergency_contact_address"))
    )
    val emergencyContact: EmergencyContact? = null,

    @Column(name = "enrolled_at", nullable = false)
    val enrolledAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L,

    @Column(name = "graduated_at")
    var graduatedAt: Instant? = null,

    // Relationship to class leadership positions (class-specific via junction table)
    // Note: Leadership is class-specific, so this is accessed via StudentClassLeadership junction entity
    // Using @OneToMany for ORM convenience, but querying should use StudentClassLeadershipRepository
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    val leadershipPositions: List<StudentClassLeadership> = emptyList()
) {
    init {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12 (K12 system), got: $gradeLevel"
        }
    }

    fun promoteToNextGrade() {
        require(gradeLevel < 12) { "Cannot promote beyond grade 12" }
        gradeLevel++
        updatedAt = Instant.now()
    }

    fun updateEnrollmentStatus(status: EnrollmentStatus) {
        enrollmentStatus = status
        updatedAt = Instant.now()
        if (status == EnrollmentStatus.GRADUATED) {
            graduatedAt = Instant.now()
        }
    }

    fun getFullName(): String = "$firstName $lastName"
}

