package com.visor.school.assessment.repository

import com.visor.school.assessment.model.ReportSubmission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReportSubmissionRepository : JpaRepository<ReportSubmission, UUID> {
    fun findByClassId(classId: UUID): List<ReportSubmission>
    fun findBySubmittedBy(submittedBy: UUID): List<ReportSubmission>
    fun findByCollectionId(collectionId: UUID): List<ReportSubmission>
}

