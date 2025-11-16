package com.visor.school.assessment.repository

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AssessmentRepository : JpaRepository<Assessment, UUID> {
    fun findByClassId(classId: UUID): List<Assessment>
    fun findByStatus(status: AssessmentStatus): List<Assessment>
    fun findByClassIdAndStatus(classId: UUID, status: AssessmentStatus): List<Assessment>
}

