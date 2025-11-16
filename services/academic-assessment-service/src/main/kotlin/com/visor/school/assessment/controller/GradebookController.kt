package com.visor.school.assessment.controller

import com.visor.school.assessment.service.GradebookService
import com.visor.school.common.api.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Gradebook controller
 * Accessible by ADMINISTRATOR, TEACHER, STUDENT, and PARENT roles
 */
@RestController
@RequestMapping("/api/v1/gradebooks")
class GradebookController(
    private val gradebookService: GradebookService
) {

    /**
     * Get class gradebook with all assessments and student grades
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    fun getClassGradebook(@PathVariable classId: UUID): ResponseEntity<ApiResponse<ClassGradebookResponse>> {
        val gradebook = gradebookService.getClassGradebook(classId)
        return ResponseEntity.ok(ApiResponse.success(ClassGradebookResponse.from(gradebook)))
    }

    /**
     * Get student gradebook with all assessments and grades
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    fun getStudentGradebook(@PathVariable studentId: UUID): ResponseEntity<ApiResponse<StudentGradebookResponse>> {
        val gradebook = gradebookService.getStudentGradebook(studentId)
        return ResponseEntity.ok(ApiResponse.success(StudentGradebookResponse.from(gradebook)))
    }
}

data class ClassGradebookResponse(
    val classId: UUID,
    val assessments: List<Map<String, Any>>,
    val studentGrades: Map<String, List<Map<String, Any>>>,
    val studentAverages: Map<String, Any>
) {
    companion object {
        fun from(gradebook: com.visor.school.assessment.service.ClassGradebook): ClassGradebookResponse {
            return ClassGradebookResponse(
                classId = gradebook.classId,
                assessments = gradebook.assessments.map { assessment ->
                    mapOf(
                        "id" to assessment.id.toString(),
                        "name" to assessment.name,
                        "type" to assessment.type.name,
                        "totalPoints" to assessment.totalPoints.toString(),
                        "status" to assessment.status.name
                    )
                },
                studentGrades = gradebook.studentGrades.entries.associate { (studentId, grades) ->
                    studentId.toString() to grades.map { grade ->
                        mapOf<String, Any>(
                            "id" to grade.id.toString(),
                            "assessmentId" to grade.assessmentId.toString(),
                            "score" to grade.score,
                            "percentage" to grade.percentage,
                            "letterGrade" to (grade.letterGrade ?: "")
                        )
                    }
                },
                studentAverages = gradebook.studentAverages.entries.associate { (studentId, average) ->
                    studentId.toString() to average
                }
            )
        }
    }
}

data class StudentGradebookResponse(
    val studentId: UUID,
    val grades: List<Map<String, Any>>,
    val average: Any
) {
    companion object {
        fun from(gradebook: com.visor.school.assessment.service.StudentGradebook): StudentGradebookResponse {
            return StudentGradebookResponse(
                studentId = gradebook.studentId,
                grades = gradebook.grades.map { grade ->
                    mapOf<String, Any>(
                        "id" to grade.id.toString(),
                        "assessmentId" to grade.assessmentId.toString(),
                        "score" to grade.score,
                        "totalPoints" to grade.totalPoints,
                        "percentage" to grade.percentage,
                        "letterGrade" to (grade.letterGrade ?: ""),
                        "recordedAt" to grade.recordedAt
                    )
                },
                average = gradebook.average
            )
        }
    }
}

