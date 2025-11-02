package com.visor.school.user.domain.model

import com.visor.school.common.constant.Constants
import com.visor.school.persistence.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
@Table(
    name = "students",
    indexes = [Index(name = "idx_student_number", columnList = "student_number", unique = true)]
)
class Student : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(name = "student_number", nullable = false, unique = true)
    @field:NotBlank(message = "Student number is required")
    var studentNumber: String = ""

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    var gender: Gender? = null

    @Column(name = "address")
    @field:Size(max = 255)
    var address: String? = null

    @Column(name = "phone")
    @field:Size(max = 20)
    var phone: String? = null

    @Column(name = "parent_guardian_name")
    @field:Size(max = Constants.MAX_NAME_LENGTH)
    var parentGuardianName: String? = null

    @Column(name = "parent_guardian_email")
    var parentGuardianEmail: String? = null

    @Column(name = "parent_guardian_phone")
    @field:Size(max = 20)
    var parentGuardianPhone: String? = null

    @Column(name = "admission_date")
    var admissionDate: LocalDate? = null

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: StudentStatus = StudentStatus.ACTIVE

    @Column(name = "photo_url")
    var photoUrl: String? = null
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

enum class StudentStatus {
    ACTIVE,
    GRADUATED,
    TRANSFERRED,
    DROPPED,
    SUSPENDED
}

