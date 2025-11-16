package com.visor.school.academicservice.controller

import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.service.ClassService
import com.visor.school.common.api.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Class management controller
 * Requires ADMINISTRATOR role or MANAGE_HOMEROOM permission for homeroom classes
 */
@RestController
@RequestMapping("/api/v1/classes")
class ClassController(
    private val classService: ClassService
) {

    /**
     * Create a homeroom class (grades 1-6 only)
     */
    @PostMapping("/homeroom")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'MANAGE_HOMEROOM')")
    fun createHomeroomClass(@Valid @RequestBody request: CreateHomeroomClassRequest): ResponseEntity<ApiResponse<ClassResponse>> {
        val schedule = request.schedule?.let {
            Schedule(
                daysOfWeek = it.daysOfWeek,
                startTime = it.startTime,
                endTime = it.endTime,
                room = it.room
            )
        }

        val classEntity = classService.createHomeroomClass(
            className = request.className,
            gradeLevel = request.gradeLevel,
            homeroomTeacherId = request.homeroomTeacherId,
            academicYear = request.academicYear,
            term = request.term,
            schedule = schedule,
            maxCapacity = request.maxCapacity,
            startDate = request.startDate,
            endDate = request.endDate
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ClassResponse.from(classEntity), "Homeroom class created successfully"))
    }

    /**
     * Create a subject class (all grades)
     */
    @PostMapping("/subject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun createSubjectClass(@Valid @RequestBody request: CreateSubjectClassRequest): ResponseEntity<ApiResponse<ClassResponse>> {
        val schedule = request.schedule?.let {
            Schedule(
                daysOfWeek = it.daysOfWeek,
                startTime = it.startTime,
                endTime = it.endTime,
                room = it.room
            )
        }

        val classEntity = classService.createSubjectClass(
            className = request.className,
            subject = request.subject,
            gradeLevel = request.gradeLevel,
            academicYear = request.academicYear,
            term = request.term,
            schedule = schedule,
            maxCapacity = request.maxCapacity,
            startDate = request.startDate,
            endDate = request.endDate
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ClassResponse.from(classEntity), "Subject class created successfully"))
    }

    /**
     * Assign class teacher/coordinator (grades 7-12 only)
     */
    @PostMapping("/{classId}/class-teacher")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun assignClassTeacher(
        @PathVariable classId: UUID,
        @Valid @RequestBody request: AssignClassTeacherRequest
    ): ResponseEntity<ApiResponse<ClassResponse>> {
        val classEntity = classService.assignClassTeacher(
            classId = classId,
            teacherId = request.teacherId,
            assignedBy = request.assignedBy
        )

        return ResponseEntity.ok(ApiResponse.success(ClassResponse.from(classEntity), "Class teacher assigned successfully"))
    }

    /**
     * Get class by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getClass(@PathVariable id: UUID): ResponseEntity<ApiResponse<ClassResponse>> {
        val classEntity = classService.getClassById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(ClassResponse.from(classEntity)))
    }

    /**
     * Get classes by grade level
     */
    @GetMapping("/grade/{gradeLevel}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getClassesByGradeLevel(@PathVariable @Min(1) @Max(12) gradeLevel: Int): ResponseEntity<ApiResponse<List<ClassResponse>>> {
        val classes = classService.getClassesByGradeLevel(gradeLevel)
        return ResponseEntity.ok(ApiResponse.success(classes.map { ClassResponse.from(it) }))
    }

    /**
     * Update class status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun updateClassStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateClassStatusRequest
    ): ResponseEntity<ApiResponse<ClassResponse>> {
        val classEntity = classService.updateClassStatus(id, request.status)
        return ResponseEntity.ok(ApiResponse.success(ClassResponse.from(classEntity), "Class status updated"))
    }
}

data class CreateHomeroomClassRequest(
    @field:NotBlank
    val className: String,

    @field:Min(1)
    @field:Max(6)
    val gradeLevel: Int,

    @field:NotNull
    val homeroomTeacherId: UUID,

    @field:NotBlank
    val academicYear: String,

    @field:NotNull
    val term: Term,

    val schedule: ScheduleRequest? = null,
    val maxCapacity: Int? = null,

    @field:NotNull
    val startDate: LocalDate,

    val endDate: LocalDate? = null
)

data class CreateSubjectClassRequest(
    @field:NotBlank
    val className: String,

    @field:NotBlank
    val subject: String,

    @field:Min(1)
    @field:Max(12)
    val gradeLevel: Int,

    @field:NotBlank
    val academicYear: String,

    @field:NotNull
    val term: Term,

    val schedule: ScheduleRequest? = null,
    val maxCapacity: Int? = null,

    @field:NotNull
    val startDate: LocalDate,

    val endDate: LocalDate? = null
)

data class AssignClassTeacherRequest(
    @field:NotNull
    val teacherId: UUID,
    val assignedBy: UUID? = null
)

data class UpdateClassStatusRequest(
    @field:NotNull
    val status: ClassStatus
)

data class ScheduleRequest(
    @field:NotBlank
    val daysOfWeek: String,

    @field:NotNull
    val startTime: LocalTime,

    @field:NotNull
    val endTime: LocalTime,

    val room: String? = null
)

data class ClassResponse(
    val id: UUID,
    val className: String,
    val classType: String,
    val subject: String?,
    val gradeLevel: Int,
    val homeroomTeacherId: UUID?,
    val classTeacherId: UUID?,
    val academicYear: String,
    val term: String,
    val maxCapacity: Int?,
    val currentEnrollment: Int,
    val status: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
) {
    companion object {
        fun from(classEntity: Class): ClassResponse {
            return ClassResponse(
                id = classEntity.id,
                className = classEntity.className,
                classType = classEntity.classType.name,
                subject = classEntity.subject,
                gradeLevel = classEntity.gradeLevel,
                homeroomTeacherId = classEntity.homeroomTeacherId,
                classTeacherId = classEntity.classTeacherId,
                academicYear = classEntity.academicYear,
                term = classEntity.term.name,
                maxCapacity = classEntity.maxCapacity,
                currentEnrollment = classEntity.currentEnrollment,
                status = classEntity.status.name,
                startDate = classEntity.startDate,
                endDate = classEntity.endDate
            )
        }
    }
}

