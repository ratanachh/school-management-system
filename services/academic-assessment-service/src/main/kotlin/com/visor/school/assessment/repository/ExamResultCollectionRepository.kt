package com.visor.school.assessment.repository

import com.visor.school.assessment.model.ExamResultCollection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExamResultCollectionRepository : JpaRepository<ExamResultCollection, UUID> {
    fun findByClassIdAndAcademicYearAndTerm(
        classId: UUID,
        academicYear: String,
        term: String
    ): List<ExamResultCollection>

    fun findByCollectedBy(collectedBy: UUID): List<ExamResultCollection>
}

