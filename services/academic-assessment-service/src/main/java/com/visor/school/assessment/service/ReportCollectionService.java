package com.visor.school.assessment.service;

import com.visor.school.assessment.event.ReportEventPublisher;
import com.visor.school.assessment.model.ExamResultCollection;
import com.visor.school.assessment.model.ExamResultCollectionStatus;
import com.visor.school.assessment.model.Grade;
import com.visor.school.assessment.model.ReportSubmission;
import com.visor.school.assessment.repository.ExamResultCollectionRepository;
import com.visor.school.assessment.repository.GradeRepository;
import com.visor.school.assessment.repository.ReportSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report collection service for class teachers (grades 7-12)
 * Requires COLLECT_EXAM_RESULTS and SUBMIT_REPORTS permissions
 */
@Service
@Transactional
public class ReportCollectionService {
    private static final Logger logger = LoggerFactory.getLogger(ReportCollectionService.class);
    
    private final ExamResultCollectionRepository examResultCollectionRepository;
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final GradeRepository gradeRepository;
    private final ReportEventPublisher reportEventPublisher;

    public ReportCollectionService(ExamResultCollectionRepository examResultCollectionRepository,
                                  ReportSubmissionRepository reportSubmissionRepository,
                                  GradeRepository gradeRepository,
                                  ReportEventPublisher reportEventPublisher) {
        this.examResultCollectionRepository = examResultCollectionRepository;
        this.reportSubmissionRepository = reportSubmissionRepository;
        this.gradeRepository = gradeRepository;
        this.reportEventPublisher = reportEventPublisher;
    }

    /**
     * Collect exam results from all subject teachers for a class
     * Requires COLLECT_EXAM_RESULTS permission
     * Only class teachers (grades 7-12) can collect exam results
     */
    public ExamResultCollection collectExamResults(
            UUID classId,
            UUID classTeacherId,
            String academicYear,
            String term) {
        logger.info("Collecting exam results for class: {} by teacher: {}", classId, classTeacherId);

        // Validate: Only class teachers (grades 7-12) with COLLECT_EXAM_RESULTS permission can collect
        // This validation should be done at the controller level with @PreAuthorize
        // For service level, we'll assume the permission check has passed

        // Check if collection already exists
        List<ExamResultCollection> existingCollections = 
            examResultCollectionRepository.findByClassIdAndAcademicYearAndTerm(classId, academicYear, term);

        if (!existingCollections.isEmpty()) {
            // Return existing collection if still collecting
            ExamResultCollection existing = existingCollections.get(0);
            if (existing.getStatus() == ExamResultCollectionStatus.COLLECTING) {
                logger.debug("Using existing collection: {}", existing.getId());
                return existing;
            }
        }

        // Get all grades for this class
        List<Grade> grades = gradeRepository.findByClassId(classId);

        // Create new collection
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("gradeCount", String.valueOf(grades.size()));
        metadata.put("subjectCount", String.valueOf(grades.stream()
            .map(Grade::getAssessmentId)
            .distinct()
            .count()));

        ExamResultCollection collection = new ExamResultCollection(
            classId,
            classTeacherId,
            academicYear,
            term,
            metadata
        );

        ExamResultCollection saved = examResultCollectionRepository.save(collection);
        logger.info("Created exam result collection: {}", saved.getId());

        // Mark as completed if all grades are collected
        ExamResultCollection finalCollection;
        if (!grades.isEmpty()) {
            saved.setStatus(ExamResultCollectionStatus.COMPLETED);
            saved.setCompletedAt(Instant.now());
            long distinctSubjects = grades.stream().map(Grade::getAssessmentId).distinct().count();
            saved.setSummary("Collected " + grades.size() + " exam results from " + distinctSubjects + " subjects");
            finalCollection = examResultCollectionRepository.save(saved);
        } else {
            finalCollection = saved;
        }

        // Publish event
        reportEventPublisher.publishReportCollectedEvent(
            finalCollection.getId(),
            classId,
            classTeacherId,
            academicYear,
            term
        );

        return finalCollection;
    }

    /**
     * Aggregate report from collected exam results
     */
    @Transactional(readOnly = true)
    public Map<String, Object> aggregateReport(UUID collectionId) {
        logger.info("Aggregating report for collection: {}", collectionId);

        ExamResultCollection collection = examResultCollectionRepository.findById(collectionId)
            .orElseThrow(() -> new NoSuchElementException("Exam result collection not found: " + collectionId));

        if (collection.getStatus() != ExamResultCollectionStatus.COMPLETED) {
            throw new IllegalStateException("Collection must be completed before aggregating report");
        }

        // Get all grades for this collection
        List<Grade> grades = gradeRepository.findByClassId(collection.getClassId());

        // Aggregate report data
        double averageScore = grades.stream()
            .map(Grade::getScore)
            .mapToDouble(BigDecimal::doubleValue)
            .average()
            .orElse(0.0);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("collectionId", collection.getId().toString());
        reportData.put("classId", collection.getClassId().toString());
        reportData.put("academicYear", collection.getAcademicYear());
        reportData.put("term", collection.getTerm());
        reportData.put("totalGrades", grades.size());
        reportData.put("totalStudents", grades.stream().map(Grade::getStudentId).distinct().count());
        reportData.put("subjectCount", grades.stream().map(Grade::getAssessmentId).distinct().count());
        reportData.put("averageScore", averageScore);
        reportData.put("collectedAt", collection.getCollectedAt() != null ? collection.getCollectedAt().toString() : "");
        reportData.put("completedAt", collection.getCompletedAt() != null ? collection.getCompletedAt().toString() : "");

        logger.info("Aggregated report for collection: {}", collectionId);
        return reportData;
    }

    /**
     * Submit aggregated report to school administration
     * Requires SUBMIT_REPORTS permission
     * Only class teachers (grades 7-12) can submit reports
     */
    public ReportSubmission submitReport(
            UUID collectionId,
            UUID classTeacherId,
            Map<String, Object> reportData) {
        logger.info("Submitting report for collection: {} by teacher: {}", collectionId, classTeacherId);

        // Validate: Only class teachers (grades 7-12) with SUBMIT_REPORTS permission can submit
        // This validation should be done at the controller level with @PreAuthorize
        // For service level, we'll assume the permission check has passed

        ExamResultCollection collection = examResultCollectionRepository.findById(collectionId)
            .orElseThrow(() -> new NoSuchElementException("Exam result collection not found: " + collectionId));

        if (collection.getStatus() != ExamResultCollectionStatus.COMPLETED) {
            throw new IllegalStateException("Collection must be completed before submitting report");
        }

        // Create submission
        ReportSubmission submission = new ReportSubmission(
            collectionId,
            classTeacherId,
            collection.getClassId(),
            reportData
        );

        ReportSubmission saved = reportSubmissionRepository.save(submission);

        // Update collection status
        collection.setStatus(ExamResultCollectionStatus.SUBMITTED);
        collection.setSubmittedAt(Instant.now());
        examResultCollectionRepository.save(collection);

        logger.info("Submitted report: {} for collection: {}", saved.getId(), collectionId);

        // Publish event
        reportEventPublisher.publishReportSubmittedEvent(
            saved.getId(),
            collectionId,
            collection.getClassId(),
            classTeacherId
        );

        return saved;
    }
}
