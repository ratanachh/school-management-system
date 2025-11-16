package com.visor.school.assessment.controller

import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.service.GradeService
import com.visor.school.common.api.ApiResponse
import com.visor.school.common.api.Permissions
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Grade controller
 * Requires TEACHER role with MANAGE_GRADES permission or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/grades")
class GradeController(
    private val gradeService: GradeService
) {

    /**
     * Record a grade for a student on an assessment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('${Permissions.MANAGE_GRADES}'))")
    fun recordGrade(@Valid @RequestBody request: RecordGradeRequest): ResponseEntity<ApiResponse<GradeResponse>> {
        val grade = gradeService.recordGrade(
            studentId = request.studentId,
            assessmentId = request.assessmentId,
            score = request.score,
            recordedBy = request.recordedBy, // In production, get from JWT token
            notes = request.notes
        )

        return ResponseEntity.ok(ApiResponse.success(GradeResponse.from(grade)))
    }

    /**
     * Update an existing grade
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('${Permissions.MANAGE_GRADES}'))")
    fun updateGrade(@Valid @RequestBody request: UpdateGradeRequest): ResponseEntity<ApiResponse<GradeResponse>> {
        val grade = gradeService.updateGrade(
            studentId = request.studentId,
            assessmentId = request.assessmentId,
            newScore = request.newScore,
            updatedBy = request.updatedBy, // In production, get from JWT token
            notes = request.notes
        )

        return ResponseEntity.ok(ApiResponse.success(GradeResponse.from(grade)))
    }

    /**
     * Get grades for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    fun getGradesByStudent(@PathVariable studentId: UUID): ResponseEntity<ApiResponse<List<GradeResponse>>> {
        val grades = gradeService.getGradesByStudent(studentId)
        return ResponseEntity.ok(ApiResponse.success(grades.map { GradeResponse.from(it) }))
    }

    /**
     * Get grades for an assessment
     */
    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getGradesByAssessment(@PathVariable assessmentId: UUID): ResponseEntity<ApiResponse<List<GradeResponse>>> {
        val grades = gradeService.getGradesByAssessment(assessmentId)
        return ResponseEntity.ok(ApiResponse.success(grades.map { GradeResponse.from(it) }))
    }
}

data class RecordGradeRequest(
    @field:NotNull
    val studentId: UUID,

    @field:NotNull
    val assessmentId: UUID,

    @field:NotNull
    @field:PositiveOrZero
    val score: BigDecimal,

    val notes: String? = null,

    @field:NotNull
    val recordedBy: UUID // In production, get from JWT token
)

data class UpdateGradeRequest(
    @field:NotNull
    val studentId: UUID,

    @field:NotNull
    val assessmentId: UUID,

    @field:NotNull
    @field:PositiveOrZero
    val newScore: BigDecimal,

    val notes: String? = null,

    @field:NotNull
    val updatedBy: UUID // In production, get from JWT token
)

data class GradeResponse(
    val id: UUID,
    val studentId: UUID,
    val assessmentId: UUID,
    val score: String,
    val totalPoints: String,
    val percentage: String,
    val letterGrade: String?,
    val recordedBy: UUID,
    val recordedAt: String,
    val updatedAt: String,
    val updatedBy: String?,
    val notes: String?
) {
    companion object {
        fun from(grade: Grade): GradeResponse {
            return GradeResponse(
                id = grade.id,
                studentId = grade.studentId,
                assessmentId = grade.assessmentId,
                score = grade.score.toString(),
                totalPoints = grade.totalPoints.toString(),
                percentage = grade.percentage.toString(),
                letterGrade = grade.letterGrade,
                recordedBy = grade.recordedBy,
                recordedAt = grade.recordedAt.toString(),
                updatedAt = grade.updatedAt.toString(),
                updatedBy = grade.updatedBy?.toString(),
                notes = grade.notes
            )
        }
    }
}

