package com.visor.school.assessment.controller;

import com.visor.school.assessment.model.ExamResultCollection;
import com.visor.school.assessment.model.ReportSubmission;
import com.visor.school.assessment.service.ReportCollectionService;
import com.visor.school.common.api.ApiResponse;
import com.visor.school.common.api.Permissions;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Report collection controller for class teachers (grades 7-12)
 * Requires COLLECT_EXAM_RESULTS and SUBMIT_REPORTS permissions
 */
@RestController
@RequestMapping("/api/v1/classes")
public class ReportCollectionController {
    
    private final ReportCollectionService reportCollectionService;

    public ReportCollectionController(ReportCollectionService reportCollectionService) {
        this.reportCollectionService = reportCollectionService;
    }

    /**
     * Collect exam results from subject teachers for a class
     * Requires COLLECT_EXAM_RESULTS permission
     * Only class teachers (grades 7-12) can collect exam results
     */
    @GetMapping("/{classId}/exam-results")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.COLLECT_EXAM_RESULTS + "')")
    public ResponseEntity<ApiResponse<ExamResultCollectionResponse>> collectExamResults(
            @PathVariable UUID classId,
            @RequestParam String academicYear,
            @RequestParam String term,
            @RequestHeader("X-User-Id") String userId) {
        UUID classTeacherId = UUID.fromString(userId);

        ExamResultCollection collection = reportCollectionService.collectExamResults(
            classId,
            classTeacherId,
            academicYear,
            term
        );

        ExamResultCollectionResponse response = ExamResultCollectionResponse.from(collection);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Submit aggregated report to school administration
     * Requires SUBMIT_REPORTS permission
     * Only class teachers (grades 7-12) can submit reports
     */
    @PostMapping("/{classId}/reports/submit")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.SUBMIT_REPORTS + "')")
    public ResponseEntity<ApiResponse<ReportSubmissionResponse>> submitReport(
            @PathVariable UUID classId,
            @RequestBody SubmitReportRequest request,
            @RequestHeader("X-User-Id") String userId) {
        UUID classTeacherId = UUID.fromString(userId);

        // First aggregate the report
        Map<String, Object> reportData = reportCollectionService.aggregateReport(request.getCollectionId());

        // Add additional data if provided
        if (request.getAdditionalData() != null) {
            reportData.putAll(request.getAdditionalData());
        }

        // Then submit it
        ReportSubmission submission = reportCollectionService.submitReport(
            request.getCollectionId(),
            classTeacherId,
            reportData
        );

        ReportSubmissionResponse response = ReportSubmissionResponse.from(submission);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

class SubmitReportRequest {
    private UUID collectionId;
    private Map<String, Object> additionalData;

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
}

class ExamResultCollectionResponse {
    private final String id;
    private final String classId;
    private final String collectedBy;
    private final String academicYear;
    private final String term;
    private final String status;
    private final String summary;
    private final Map<String, Object> metadata;
    private final String collectedAt;
    private final String completedAt;
    private final String submittedAt;

    public ExamResultCollectionResponse(String id, String classId, String collectedBy, String academicYear,
                                       String term, String status, String summary, Map<String, Object> metadata,
                                       String collectedAt, String completedAt, String submittedAt) {
        this.id = id;
        this.classId = classId;
        this.collectedBy = collectedBy;
        this.academicYear = academicYear;
        this.term = term;
        this.status = status;
        this.summary = summary;
        this.metadata = metadata;
        this.collectedAt = collectedAt;
        this.completedAt = completedAt;
        this.submittedAt = submittedAt;
    }

    public static ExamResultCollectionResponse from(ExamResultCollection collection) {
        return new ExamResultCollectionResponse(
            collection.getId().toString(),
            collection.getClassId().toString(),
            collection.getCollectedBy().toString(),
            collection.getAcademicYear(),
            collection.getTerm(),
            collection.getStatus().name(),
            collection.getSummary(),
            collection.getMetadata(),
            collection.getCollectedAt().toString(),
            collection.getCompletedAt() != null ? collection.getCompletedAt().toString() : null,
            collection.getSubmittedAt() != null ? collection.getSubmittedAt().toString() : null
        );
    }

    // Getters
    public String getId() { return id; }
    public String getClassId() { return classId; }
    public String getCollectedBy() { return collectedBy; }
    public String getAcademicYear() { return academicYear; }
    public String getTerm() { return term; }
    public String getStatus() { return status; }
    public String getSummary() { return summary; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getCollectedAt() { return collectedAt; }
    public String getCompletedAt() { return completedAt; }
    public String getSubmittedAt() { return submittedAt; }
}

class ReportSubmissionResponse {
    private final String id;
    private final String collectionId;
    private final String submittedBy;
    private final String classId;
    private final Map<String, Object> reportData;
    private final String submittedAt;
    private final String reviewedBy;
    private final String reviewedAt;
    private final String reviewNotes;

    public ReportSubmissionResponse(String id, String collectionId, String submittedBy, String classId,
                                   Map<String, Object> reportData, String submittedAt, String reviewedBy,
                                   String reviewedAt, String reviewNotes) {
        this.id = id;
        this.collectionId = collectionId;
        this.submittedBy = submittedBy;
        this.classId = classId;
        this.reportData = reportData;
        this.submittedAt = submittedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNotes = reviewNotes;
    }

    public static ReportSubmissionResponse from(ReportSubmission submission) {
        return new ReportSubmissionResponse(
            submission.getId().toString(),
            submission.getCollectionId().toString(),
            submission.getSubmittedBy().toString(),
            submission.getClassId().toString(),
            submission.getReportData(),
            submission.getSubmittedAt().toString(),
            submission.getReviewedBy() != null ? submission.getReviewedBy().toString() : null,
            submission.getReviewedAt() != null ? submission.getReviewedAt().toString() : null,
            submission.getReviewNotes()
        );
    }

    // Getters
    public String getId() { return id; }
    public String getCollectionId() { return collectionId; }
    public String getSubmittedBy() { return submittedBy; }
    public String getClassId() { return classId; }
    public Map<String, Object> getReportData() { return reportData; }
    public String getSubmittedAt() { return submittedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public String getReviewedAt() { return reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
}
