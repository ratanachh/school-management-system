package com.visor.school.assessment.service

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.repository.AssessmentRepository
import com.visor.school.assessment.repository.GradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Gradebook service for viewing class and student gradebooks
 */
@Service
@Transactional(readOnly = true)
class GradebookService(
    private val assessmentRepository: AssessmentRepository,
    private val gradeRepository: GradeRepository,
    private val gradeCalculator: GradeCalculator
) {

    /**
     * Get class gradebook with all assessments and student grades
     */
    fun getClassGradebook(classId: UUID): ClassGradebook {
        val assessments = assessmentRepository.findByClassId(classId)
        val assessmentIds = assessments.map { it.id }

        val studentGrades = mutableMapOf<UUID, MutableList<Grade>>()

        // Get all grades for assessments in this class
        assessmentIds.forEach { assessmentId ->
            val grades = gradeRepository.findByAssessmentId(assessmentId)
            grades.forEach { grade ->
                studentGrades.getOrPut(grade.studentId) { mutableListOf() }.add(grade)
            }
        }

        // Calculate averages for each student
        val studentAverages = studentGrades.mapValues { (_, grades) ->
            gradeCalculator.calculateAverage(grades)
        }

        return ClassGradebook(
            classId = classId,
            assessments = assessments,
            studentGrades = studentGrades,
            studentAverages = studentAverages
        )
    }

    /**
     * Get student gradebook with all assessments and grades
     */
    fun getStudentGradebook(studentId: UUID): StudentGradebook {
        val grades = gradeRepository.findByStudentId(studentId)
        val assessmentIds = grades.map { it.assessmentId }.distinct()

        val assessments = assessmentIds.mapNotNull { assessmentId ->
            // Note: In a real implementation, you might want to fetch assessments from the Academic Service
            // For now, we'll use the assessment IDs from grades
            null // Placeholder - would need to fetch from Academic Service
        }

        val average = gradeCalculator.calculateAverage(grades)

        return StudentGradebook(
            studentId = studentId,
            grades = grades,
            average = average
        )
    }
}

data class ClassGradebook(
    val classId: UUID,
    val assessments: List<Assessment>,
    val studentGrades: Map<UUID, List<Grade>>,
    val studentAverages: Map<UUID, BigDecimal>
)

data class StudentGradebook(
    val studentId: UUID,
    val grades: List<Grade>,
    val average: BigDecimal
)

