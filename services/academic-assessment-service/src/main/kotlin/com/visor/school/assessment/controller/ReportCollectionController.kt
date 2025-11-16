package com.visor.school.assessment.controller

import com.visor.school.assessment.model.ExamResultCollection
import com.visor.school.assessment.model.ReportSubmission
import com.visor.school.assessment.service.ReportCollectionService
import com.visor.school.common.api.ApiResponse
import com.visor.school.common.api.Permissions
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Report collection controller for class teachers (grades 7-12)
 * Requires COLLECT_EXAM_RESULTS and SUBMIT_REPORTS permissions
 */
@RestController
@RequestMapping("/api/v1/classes")
class ReportCollectionController(
    private val reportCollectionService: ReportCollectionService
) {

    /**
     * Collect exam results from subject teachers for a class
     * Requires COLLECT_EXAM_RESULTS permission
     * Only class teachers (grades 7-12) can collect exam results
     */
    @GetMapping("/{classId}/exam-results")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.COLLECT_EXAM_RESULTS}')")
    fun collectExamResults(
        @PathVariable classId: UUID,
        @RequestParam academicYear: String,
        @RequestParam term: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<ExamResultCollectionResponse>> {
        val classTeacherId = UUID.fromString(userId)

        val collection = reportCollectionService.collectExamResults(
            classId = classId,
            classTeacherId = classTeacherId,
            academicYear = academicYear,
            term = term
        )

        val response = ExamResultCollectionResponse.from(collection)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Submit aggregated report to school administration
     * Requires SUBMIT_REPORTS permission
     * Only class teachers (grades 7-12) can submit reports
     */
    @PostMapping("/{classId}/reports/submit")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.SUBMIT_REPORTS}')")
    fun submitReport(
        @PathVariable classId: UUID,
        @RequestBody request: SubmitReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<ReportSubmissionResponse>> {
        val classTeacherId = UUID.fromString(userId)

        // First aggregate the report
        val reportData = reportCollectionService.aggregateReport(request.collectionId)

        // Then submit it
        val submission = reportCollectionService.submitReport(
            collectionId = request.collectionId,
            classTeacherId = classTeacherId,
            reportData = reportData + (request.additionalData ?: emptyMap())
        )

        val response = ReportSubmissionResponse.from(submission)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}

data class SubmitReportRequest(
    val collectionId: UUID,
    val additionalData: Map<String, Any>? = null
)

data class ExamResultCollectionResponse(
    val id: String,
    val classId: String,
    val collectedBy: String,
    val academicYear: String,
    val term: String,
    val status: String,
    val summary: String?,
    val metadata: Map<String, Any>?,
    val collectedAt: String,
    val completedAt: String?,
    val submittedAt: String?
) {
    companion object {
        fun from(collection: ExamResultCollection): ExamResultCollectionResponse {
            return ExamResultCollectionResponse(
                id = collection.id.toString(),
                classId = collection.classId.toString(),
                collectedBy = collection.collectedBy.toString(),
                academicYear = collection.academicYear,
                term = collection.term,
                status = collection.status.name,
                summary = collection.summary,
                metadata = collection.metadata,
                collectedAt = collection.collectedAt.toString(),
                completedAt = collection.completedAt?.toString(),
                submittedAt = collection.submittedAt?.toString()
            )
        }
    }
}

data class ReportSubmissionResponse(
    val id: String,
    val collectionId: String,
    val submittedBy: String,
    val classId: String,
    val reportData: Map<String, Any>,
    val submittedAt: String,
    val reviewedBy: String?,
    val reviewedAt: String?,
    val reviewNotes: String?
) {
    companion object {
        fun from(submission: ReportSubmission): ReportSubmissionResponse {
            return ReportSubmissionResponse(
                id = submission.id.toString(),
                collectionId = submission.collectionId.toString(),
                submittedBy = submission.submittedBy.toString(),
                classId = submission.classId.toString(),
                reportData = submission.reportData,
                submittedAt = submission.submittedAt.toString(),
                reviewedBy = submission.reviewedBy?.toString(),
                reviewedAt = submission.reviewedAt?.toString(),
                reviewNotes = submission.reviewNotes
            )
        }
    }
}

