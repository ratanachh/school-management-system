package com.visor.school.user.domain.model

import com.visor.school.common.constant.Constants
import com.visor.school.persistence.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
@Table(
    name = "teachers",
    indexes = [Index(name = "idx_employee_number", columnList = "employee_number", unique = true)]
)
class Teacher : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(name = "employee_number", nullable = false, unique = true)
    @field:NotBlank(message = "Employee number is required")
    var employeeNumber: String = ""

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

    @Column(name = "hire_date")
    var hireDate: LocalDate? = null

    @Column(name = "qualifications", columnDefinition = "TEXT")
    var qualifications: String? = null

    @Column(name = "specialization")
    @field:Size(max = 100)
    var specialization: String? = null

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: TeacherStatus = TeacherStatus.ACTIVE

    @Column(name = "photo_url")
    var photoUrl: String? = null
}

enum class TeacherStatus {
    ACTIVE,
    ON_LEAVE,
    RESIGNED,
    RETIRED,
    TERMINATED
}

