package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Class entity supporting both homeroom classes (grades 1-6) and subject classes (all grades)
 */
@Entity
@Table(name = "classes", indexes = [
    Index(name = "idx_classes_name", columnList = "class_name"),
    Index(name = "idx_classes_grade_level", columnList = "grade_level"),
    Index(name = "idx_classes_class_type", columnList = "class_type"),
    Index(name = "idx_classes_homeroom_teacher", columnList = "homeroom_teacher_id"),
    Index(name = "idx_classes_class_teacher", columnList = "class_teacher_id")
])
class Class(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "class_name", nullable = false)
    val className: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false)
    val classType: ClassType,

    @Column(name = "subject")
    val subject: String? = null,

    @Column(name = "grade_level", nullable = false)
    val gradeLevel: Int,

    @Column(name = "homeroom_teacher_id")
    val homeroomTeacherId: UUID? = null,

    @Column(name = "class_teacher_id")
    val classTeacherId: UUID? = null,

    @Column(name = "academic_year", nullable = false)
    val academicYear: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "term", nullable = false)
    val term: Term,

    @Embedded
    val schedule: Schedule? = null,

    @Column(name = "max_capacity")
    val maxCapacity: Int? = null,

    @Column(name = "current_enrollment", nullable = false)
    var currentEnrollment: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ClassStatus = ClassStatus.SCHEDULED,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L
) {
    init {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12 (K12 system), got: $gradeLevel"
        }

        when (classType) {
            ClassType.HOMEROOM -> {
                require(gradeLevel in 1..6) {
                    "Homeroom classes are only for grades 1-6, got: $gradeLevel"
                }
                require(subject == null) {
                    "Homeroom classes should not have a subject"
                }
                require(classTeacherId == null) {
                    "Homeroom classes should not have a class teacher (use homeroomTeacherId)"
                }
            }
            ClassType.SUBJECT -> {
                require(subject != null) {
                    "Subject classes must have a subject"
                }
                require(homeroomTeacherId == null) {
                    "Subject classes should not have a homeroom teacher"
                }
            }
        }
    }

    fun updateStatus(newStatus: ClassStatus) {
        status = newStatus
        updatedAt = Instant.now()
    }

    fun incrementEnrollment() {
        currentEnrollment++
        updatedAt = Instant.now()
    }

    fun decrementEnrollment() {
        require(currentEnrollment > 0) { "Cannot decrement enrollment below 0" }
        currentEnrollment--
        updatedAt = Instant.now()
    }
}

