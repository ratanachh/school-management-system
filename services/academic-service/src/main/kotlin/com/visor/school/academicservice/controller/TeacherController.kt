package com.visor.school.academicservice.controller

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import com.visor.school.academicservice.service.TeacherService
import com.visor.school.common.api.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Teacher management controller
 * Requires ADMINISTRATOR role for most operations
 */
@RestController
@RequestMapping("/api/v1/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {

    /**
     * Create a new teacher
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun createTeacher(@Valid @RequestBody request: CreateTeacherRequest): ResponseEntity<ApiResponse<TeacherResponse>> {
        val teacher = teacherService.createTeacher(
            userId = request.userId,
            qualifications = request.qualifications,
            subjectSpecializations = request.subjectSpecializations,
            hireDate = request.hireDate,
            department = request.department
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(TeacherResponse.from(teacher), "Teacher created successfully"))
    }

    /**
     * Get teacher by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getTeacher(@PathVariable id: UUID): ResponseEntity<ApiResponse<TeacherResponse>> {
        val teacher = teacherService.getTeacherById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(TeacherResponse.from(teacher)))
    }

    /**
     * Get teacher by user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getTeacherByUserId(@PathVariable userId: UUID): ResponseEntity<ApiResponse<TeacherResponse>> {
        val teacher = teacherService.getTeacherByUserId(userId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(TeacherResponse.from(teacher)))
    }

    /**
     * Get teachers by employment status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun getTeachersByStatus(@PathVariable status: EmploymentStatus): ResponseEntity<ApiResponse<List<TeacherResponse>>> {
        val teachers = teacherService.getTeachersByStatus(status)
        return ResponseEntity.ok(ApiResponse.success(teachers.map { TeacherResponse.from(it) }))
    }

    /**
     * Get teachers by department
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun getTeachersByDepartment(@PathVariable department: String): ResponseEntity<ApiResponse<List<TeacherResponse>>> {
        val teachers = teacherService.getTeachersByDepartment(department)
        return ResponseEntity.ok(ApiResponse.success(teachers.map { TeacherResponse.from(it) }))
    }

    /**
     * Update employment status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun updateEmploymentStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTeacherStatusRequest
    ): ResponseEntity<ApiResponse<TeacherResponse>> {
        val teacher = teacherService.updateEmploymentStatus(id, request.status)
        return ResponseEntity.ok(ApiResponse.success(TeacherResponse.from(teacher), "Employment status updated"))
    }
}

data class CreateTeacherRequest(
    @field:NotNull
    val userId: UUID,

    val qualifications: List<String> = emptyList(),

    @field:NotEmpty
    val subjectSpecializations: List<String>,

    @field:NotNull
    val hireDate: LocalDate,

    val department: String? = null
)

data class UpdateTeacherStatusRequest(
    @field:NotNull
    val status: EmploymentStatus
)

data class TeacherResponse(
    val id: UUID,
    val employeeId: String,
    val userId: UUID,
    val qualifications: List<String>,
    val subjectSpecializations: List<String>,
    val hireDate: LocalDate,
    val employmentStatus: String,
    val department: String? = null
) {
    companion object {
        fun from(teacher: Teacher): TeacherResponse {
            return TeacherResponse(
                id = teacher.id,
                employeeId = teacher.employeeId,
                userId = teacher.userId,
                qualifications = teacher.qualifications,
                subjectSpecializations = teacher.subjectSpecializations,
                hireDate = teacher.hireDate,
                employmentStatus = teacher.employmentStatus.name,
                department = teacher.department
            )
        }
    }
}
