package com.visor.school.academicservice.controller

import com.visor.school.academicservice.model.Address
import com.visor.school.academicservice.model.EmergencyContact
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.service.StudentService
import com.visor.school.common.api.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Student management controller
 * Requires ADMINISTRATOR role or VIEW_ALL_STUDENTS permission for most operations
 */
@RestController
@RequestMapping("/api/v1/students")
class StudentController(
    private val studentService: StudentService
) {

    /**
     * Enroll a new student
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun enrollStudent(@Valid @RequestBody request: EnrollStudentRequest): ResponseEntity<ApiResponse<StudentResponse>> {
        val address = request.address?.let {
            Address(
                street = it.street,
                city = it.city,
                state = it.state,
                postalCode = it.postalCode,
                country = it.country ?: "Cambodia"
            )
        }

        val emergencyContact = request.emergencyContact?.let {
            EmergencyContact(
                name = it.name,
                relationship = it.relationship,
                phoneNumber = it.phoneNumber,
                email = it.email,
                address = it.address
            )
        }

        val student = studentService.enrollStudent(
            userId = request.userId,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            gradeLevel = request.gradeLevel,
            address = address,
            emergencyContact = emergencyContact
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(StudentResponse.from(student), "Student enrolled successfully"))
    }

    /**
     * Get student by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    fun getStudent(@PathVariable id: UUID): ResponseEntity<ApiResponse<StudentResponse>> {
        val student = studentService.getStudentById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(StudentResponse.from(student)))
    }

    /**
     * Get student by student ID
     */
    @GetMapping("/student-id/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    fun getStudentByStudentId(@PathVariable studentId: String): ResponseEntity<ApiResponse<StudentResponse>> {
        val student = studentService.getStudentByStudentId(studentId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(StudentResponse.from(student)))
    }

    /**
     * Search students by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    fun searchStudents(@RequestParam name: String): ResponseEntity<ApiResponse<List<StudentResponse>>> {
        val students = studentService.searchStudentsByName(name)
        return ResponseEntity.ok(ApiResponse.success(students.map { StudentResponse.from(it) }))
    }

    /**
     * Get students by grade level
     */
    @GetMapping("/grade/{gradeLevel}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    fun getStudentsByGradeLevel(@PathVariable @Min(1) @Max(12) gradeLevel: Int): ResponseEntity<ApiResponse<List<StudentResponse>>> {
        val students = studentService.getStudentsByGradeLevel(gradeLevel)
        return ResponseEntity.ok(ApiResponse.success(students.map { StudentResponse.from(it) }))
    }

    /**
     * Update student information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun updateStudent(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStudentRequest
    ): ResponseEntity<ApiResponse<StudentResponse>> {
        val address = request.address?.let {
            Address(
                street = it.street,
                city = it.city,
                state = it.state,
                postalCode = it.postalCode,
                country = it.country ?: "Cambodia"
            )
        }

        val emergencyContact = request.emergencyContact?.let {
            EmergencyContact(
                name = it.name,
                relationship = it.relationship,
                phoneNumber = it.phoneNumber,
                email = it.email,
                address = it.address
            )
        }

        val student = studentService.updateStudent(
            id = id,
            firstName = request.firstName,
            lastName = request.lastName,
            gradeLevel = request.gradeLevel,
            address = address,
            emergencyContact = emergencyContact
        )

        return ResponseEntity.ok(ApiResponse.success(StudentResponse.from(student), "Student updated successfully"))
    }

    /**
     * Update enrollment status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun updateEnrollmentStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStudentStatusRequest
    ): ResponseEntity<ApiResponse<StudentResponse>> {
        val student = studentService.updateEnrollmentStatus(id, request.status)
        return ResponseEntity.ok(ApiResponse.success(StudentResponse.from(student), "Enrollment status updated"))
    }

    /**
     * Promote student to next grade
     */
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun promoteStudent(@PathVariable id: UUID): ResponseEntity<ApiResponse<StudentResponse>> {
        val student = studentService.promoteStudent(id)
        return ResponseEntity.ok(ApiResponse.success(StudentResponse.from(student), "Student promoted successfully"))
    }
}

// Request DTOs
data class EnrollStudentRequest(
    @field:NotNull
    val userId: UUID,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    @field:NotNull
    val dateOfBirth: LocalDate,

    @field:Min(1)
    @field:Max(12)
    val gradeLevel: Int,

    val address: AddressRequest? = null,
    val emergencyContact: EmergencyContactRequest? = null
)

data class UpdateStudentRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    @field:Min(1)
    @field:Max(12)
    val gradeLevel: Int? = null,
    val address: AddressRequest? = null,
    val emergencyContact: EmergencyContactRequest? = null
)

data class UpdateStudentStatusRequest(
    @field:NotNull
    val status: EnrollmentStatus
)

data class AddressRequest(
    @field:NotBlank
    val street: String,

    @field:NotBlank
    val city: String,

    val state: String? = null,

    @field:NotBlank
    val postalCode: String,

    val country: String? = "Cambodia"
)

data class EmergencyContactRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val relationship: String,

    @field:NotBlank
    val phoneNumber: String,

    val email: String? = null,
    val address: String? = null
)

// Response DTOs
data class StudentResponse(
    val id: UUID,
    val studentId: String,
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gradeLevel: Int,
    val enrollmentStatus: String,
    val address: AddressRequest? = null,
    val emergencyContact: EmergencyContactRequest? = null
) {
    companion object {
        fun from(student: Student): StudentResponse {
            return StudentResponse(
                id = student.id,
                studentId = student.studentId,
                userId = student.userId,
                firstName = student.firstName,
                lastName = student.lastName,
                dateOfBirth = student.dateOfBirth,
                gradeLevel = student.gradeLevel,
                enrollmentStatus = student.enrollmentStatus.name,
                address = student.address?.let {
                    AddressRequest(
                        street = it.street,
                        city = it.city,
                        state = it.state,
                        postalCode = it.postalCode,
                        country = it.country
                    )
                },
                emergencyContact = student.emergencyContact?.let {
                    EmergencyContactRequest(
                        name = it.name,
                        relationship = it.relationship,
                        phoneNumber = it.phoneNumber,
                        email = it.email,
                        address = it.address
                    )
                }
            )
        }
    }
}
