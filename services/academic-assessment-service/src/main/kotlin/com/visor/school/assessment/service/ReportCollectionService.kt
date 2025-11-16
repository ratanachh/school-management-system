package com.visor.school.assessment.service

import com.visor.school.assessment.event.ReportEventPublisher
import com.visor.school.assessment.model.ExamResultCollection
import com.visor.school.assessment.model.ExamResultCollectionStatus
import com.visor.school.assessment.model.ReportSubmission
import com.visor.school.assessment.repository.ExamResultCollectionRepository
import com.visor.school.assessment.repository.GradeRepository
import com.visor.school.assessment.repository.ReportSubmissionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Report collection service for class teachers (grades 7-12)
 * Requires COLLECT_EXAM_RESULTS and SUBMIT_REPORTS permissions
 */
@Service
@Transactional
class ReportCollectionService(
    private val examResultCollectionRepository: ExamResultCollectionRepository,
    private val reportSubmissionRepository: ReportSubmissionRepository,
    private val gradeRepository: GradeRepository,
    private val reportEventPublisher: ReportEventPublisher
) {
    private val logger = LoggerFactory.getLogger(ReportCollectionService::class.java)

    /**
     * Collect exam results from all subject teachers for a class
     * Requires COLLECT_EXAM_RESULTS permission
     * Only class teachers (grades 7-12) can collect exam results
     */
    fun collectExamResults(
        classId: UUID,
        classTeacherId: UUID,
        academicYear: String,
        term: String
    ): ExamResultCollection {
        logger.info("Collecting exam results for class: $classId by teacher: $classTeacherId")

        // Validate: Only class teachers (grades 7-12) with COLLECT_EXAM_RESULTS permission can collect
        // This validation should be done at the controller level with @PreAuthorize
        // For service level, we'll assume the permission check has passed

        // Check if collection already exists
        val existingCollections = examResultCollectionRepository.findByClassIdAndAcademicYearAndTerm(
            classId, academicYear, term
        )

        if (existingCollections.isNotEmpty()) {
            // Return existing collection if still collecting
            val existing = existingCollections.first()
            if (existing.status == ExamResultCollectionStatus.COLLECTING) {
                logger.debug("Using existing collection: ${existing.id}")
                return existing
            }
        }

        // Get all grades for this class
        // Note: academicYear and term are stored in ExamResultCollection as metadata
        // The actual filtering by academicYear/term should be done at the Class level
        // For now, we collect all grades for the class
        val grades = gradeRepository.findByClassId(classId)

        // Create new collection
        val collection = ExamResultCollection(
            classId = classId,
            collectedBy = classTeacherId,
            academicYear = academicYear,
            term = term,
            status = ExamResultCollectionStatus.COLLECTING,
            metadata = mapOf(
                "gradeCount" to grades.size.toString(),
                "subjectCount" to grades.map { it.assessmentId }.distinct().size.toString()
            )
        )

        val saved = examResultCollectionRepository.save(collection)
        logger.info("Created exam result collection: ${saved.id}")

        // Mark as completed if all grades are collected
        val finalCollection = if (grades.isNotEmpty()) {
            saved.status = ExamResultCollectionStatus.COMPLETED
            saved.completedAt = Instant.now()
            saved.summary = "Collected ${grades.size} exam results from ${grades.map { it.assessmentId }.distinct().size} subjects"
            examResultCollectionRepository.save(saved)
        } else {
            saved
        }

        // Publish event
        reportEventPublisher.publishReportCollectedEvent(
            collectionId = finalCollection.id,
            classId = classId,
            classTeacherId = classTeacherId,
            academicYear = academicYear,
            term = term
        )

        return finalCollection
    }

    /**
     * Aggregate report from collected exam results
     */
    @Transactional(readOnly = true)
    fun aggregateReport(collectionId: UUID): Map<String, Any> {
        logger.info("Aggregating report for collection: $collectionId")

        val collection = examResultCollectionRepository.findById(collectionId)
            .orElseThrow { NoSuchElementException("Exam result collection not found: $collectionId") }

        if (collection.status != ExamResultCollectionStatus.COMPLETED) {
            throw IllegalStateException("Collection must be completed before aggregating report")
        }

        // Get all grades for this collection
        val grades = gradeRepository.findByClassId(collection.classId)

        // Aggregate report data
        val reportData = mapOf<String, Any>(
            "collectionId" to collection.id.toString(),
            "classId" to collection.classId.toString(),
            "academicYear" to collection.academicYear,
            "term" to collection.term,
            "totalGrades" to grades.size,
            "totalStudents" to grades.map { it.studentId }.distinct().size,
            "subjectCount" to grades.map { it.assessmentId }.distinct().size,
            "averageScore" to grades.map { it.score.toDouble() }.average().let { if (it.isNaN()) 0.0 else it },
            "collectedAt" to collection.collectedAt?.toString().orEmpty(),
            "completedAt" to (collection.completedAt?.toString() ?: "")
        )

        logger.info("Aggregated report for collection: $collectionId")
        return reportData
    }

    /**
     * Submit aggregated report to school administration
     * Requires SUBMIT_REPORTS permission
     * Only class teachers (grades 7-12) can submit reports
     */
    fun submitReport(
        collectionId: UUID,
        classTeacherId: UUID,
        reportData: Map<String, Any>
    ): ReportSubmission {
        logger.info("Submitting report for collection: $collectionId by teacher: $classTeacherId")

        // Validate: Only class teachers (grades 7-12) with SUBMIT_REPORTS permission can submit
        // This validation should be done at the controller level with @PreAuthorize
        // For service level, we'll assume the permission check has passed

        val collection = examResultCollectionRepository.findById(collectionId)
            .orElseThrow { NoSuchElementException("Exam result collection not found: $collectionId") }

        if (collection.status != ExamResultCollectionStatus.COMPLETED) {
            throw IllegalStateException("Collection must be completed before submitting report")
        }

        // Create submission
        val submission = ReportSubmission(
            collectionId = collectionId,
            submittedBy = classTeacherId,
            classId = collection.classId,
            reportData = reportData
        )

        val saved = reportSubmissionRepository.save(submission)

        // Update collection status
        collection.status = ExamResultCollectionStatus.SUBMITTED
        collection.submittedAt = Instant.now()
        examResultCollectionRepository.save(collection)

        logger.info("Submitted report: ${saved.id} for collection: $collectionId")

        // Publish event
        reportEventPublisher.publishReportSubmittedEvent(
            submissionId = saved.id,
            collectionId = collectionId,
            classId = collection.classId,
            classTeacherId = classTeacherId
        )

        return saved
    }
}

