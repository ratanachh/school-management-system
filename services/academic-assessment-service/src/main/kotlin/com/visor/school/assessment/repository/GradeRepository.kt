package com.visor.school.assessment.repository

import com.visor.school.assessment.model.Grade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface GradeRepository : JpaRepository<Grade, UUID> {
    fun findByStudentId(studentId: UUID): List<Grade>
    fun findByAssessmentId(assessmentId: UUID): List<Grade>
    fun findByStudentIdAndAssessmentId(studentId: UUID, assessmentId: UUID): Optional<Grade>
    fun findByStudentIdAndAssessmentIdIn(studentId: UUID, assessmentIds: List<UUID>): List<Grade>
    
    @Query("SELECT g FROM Grade g JOIN Assessment a ON g.assessmentId = a.id WHERE a.classId = :classId")
    fun findByClassId(
        @Param("classId") classId: UUID
    ): List<Grade>
}

