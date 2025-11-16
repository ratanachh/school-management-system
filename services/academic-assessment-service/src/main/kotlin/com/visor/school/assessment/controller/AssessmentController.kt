package com.visor.school.assessment.controller

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.service.AssessmentService
import com.visor.school.common.api.ApiResponse
import com.visor.school.common.api.Permissions
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Assessment controller
 * Requires TEACHER role with MANAGE_GRADES permission or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/assessments")
class AssessmentController(
    private val assessmentService: AssessmentService
) {

    /**
     * Create a new assessment
     */
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('${com.visor.school.common.api.Permissions.MANAGE_GRADES}'))")
    fun createAssessment(@Valid @RequestBody request: CreateAssessmentRequest): ResponseEntity<ApiResponse<AssessmentResponse>> {
        val assessment = assessmentService.createAssessment(
            classId = request.classId,
            name = request.name,
            type = AssessmentType.valueOf(request.type),
            totalPoints = request.totalPoints,
            createdBy = request.createdBy, // In production, get from JWT token
            description = request.description,
            weight = request.weight,
            dueDate = request.dueDate?.let { LocalDate.parse(it) }
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AssessmentResponse.from(assessment)))
    }

    /**
     * Get assessment by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    fun getAssessment(@PathVariable id: UUID): ResponseEntity<ApiResponse<AssessmentResponse>> {
        val assessment = assessmentService.getAssessment(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(AssessmentResponse.from(assessment)))
    }

    /**
     * Get assessments by class ID
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    fun getAssessmentsByClass(@PathVariable classId: UUID): ResponseEntity<ApiResponse<List<AssessmentResponse>>> {
        val assessments = assessmentService.getAssessmentsByClass(classId)
        return ResponseEntity.ok(ApiResponse.success(assessments.map { AssessmentResponse.from(it) }))
    }

    /**
     * Publish an assessment
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('${Permissions.MANAGE_GRADES}'))")
    fun publishAssessment(@PathVariable id: UUID): ResponseEntity<ApiResponse<AssessmentResponse>> {
        val assessment = assessmentService.publishAssessment(id)
        return ResponseEntity.ok(ApiResponse.success(AssessmentResponse.from(assessment)))
    }
}

data class CreateAssessmentRequest(
    @field:NotNull
    val classId: UUID,

    @field:NotBlank
    val name: String,

    @field:NotNull
    val type: String,

    @field:NotNull
    @field:Positive
    val totalPoints: BigDecimal,

    val description: String? = null,

    val weight: BigDecimal? = null,

    val dueDate: String? = null,

    @field:NotNull
    val createdBy: UUID // In production, get from JWT token
)

data class AssessmentResponse(
    val id: UUID,
    val classId: UUID,
    val name: String,
    val type: String,
    val description: String?,
    val totalPoints: String,
    val weight: String?,
    val dueDate: String?,
    val createdBy: UUID,
    val status: String,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(assessment: Assessment): AssessmentResponse {
            return AssessmentResponse(
                id = assessment.id,
                classId = assessment.classId,
                name = assessment.name,
                type = assessment.type.name,
                description = assessment.description,
                totalPoints = assessment.totalPoints.toString(),
                weight = assessment.weight?.toString(),
                dueDate = assessment.dueDate?.toString(),
                createdBy = assessment.createdBy,
                status = assessment.status.name,
                createdAt = assessment.createdAt.toString(),
                updatedAt = assessment.updatedAt.toString()
            )
        }
    }
}

