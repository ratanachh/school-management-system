package com.visor.school.academicservice.service

import com.visor.school.academicservice.event.AcademicRecordEventPublisher
import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.academicservice.model.AcademicStanding
import com.visor.school.academicservice.repository.AcademicRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Academic record service for managing student academic history
 */
@Service
@Transactional
class AcademicRecordService(
    private val academicRecordRepository: AcademicRecordRepository,
    private val gpaCalculator: GPACalculator,
    private val transcriptGenerator: TranscriptGenerator,
    private val academicRecordEventPublisher: AcademicRecordEventPublisher
) {
    private val logger = LoggerFactory.getLogger(AcademicRecordService::class.java)

    /**
     * Get academic record by student ID
     */
    @Transactional(readOnly = true)
    fun getAcademicRecord(studentId: UUID): AcademicRecord? {
        return academicRecordRepository.findByStudentId(studentId).orElse(null)
    }

    /**
     * Get or create academic record for a student
     */
    fun getOrCreateAcademicRecord(studentId: UUID): AcademicRecord {
        val existing = academicRecordRepository.findByStudentId(studentId)
        if (existing.isPresent) {
            return existing.get()
        }

        val newRecord = AcademicRecord(
            studentId = studentId,
            currentGPA = BigDecimal.ZERO,
            cumulativeGPA = BigDecimal.ZERO,
            creditsEarned = 0,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        val saved = academicRecordRepository.save(newRecord)
        logger.info("Created academic record for student: $studentId")
        return saved
    }

    /**
     * Generate transcript PDF for a student
     */
    @Transactional(readOnly = true)
    fun generateTranscript(studentId: UUID): ByteArray {
        val record = academicRecordRepository.findByStudentId(studentId)
            .orElseThrow { IllegalArgumentException("Academic record not found for student: $studentId") }

        logger.info("Generating transcript for student: $studentId")
        return transcriptGenerator.generate(record)
    }

    /**
     * Update GPA for a student's academic record
     */
    fun updateGPA(
        studentId: UUID,
        currentGPA: BigDecimal,
        cumulativeGPA: BigDecimal
    ): AcademicRecord {
        val record = academicRecordRepository.findByStudentId(studentId)
            .orElseThrow { IllegalArgumentException("Academic record not found for student: $studentId") }

        record.updateGPA(currentGPA, cumulativeGPA)
        val saved = academicRecordRepository.save(record)

        logger.info("Updated GPA for student $studentId: current=$currentGPA, cumulative=$cumulativeGPA")

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved)

        return saved
    }

    /**
     * Recalculate GPA from completed courses
     */
    fun recalculateGPA(studentId: UUID): AcademicRecord {
        val record = academicRecordRepository.findByStudentId(studentId)
            .orElseThrow { IllegalArgumentException("Academic record not found for student: $studentId") }

        val calculatedGPA = gpaCalculator.calculateGPA(record.completedCourses)
        record.updateGPA(calculatedGPA, calculatedGPA)

        val saved = academicRecordRepository.save(record)
        logger.info("Recalculated GPA for student $studentId: $calculatedGPA")

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved)

        return saved
    }

    /**
     * Update academic standing
     */
    fun updateAcademicStanding(studentId: UUID, standing: AcademicStanding): AcademicRecord {
        val record = academicRecordRepository.findByStudentId(studentId)
            .orElseThrow { IllegalArgumentException("Academic record not found for student: $studentId") }

        record.updateAcademicStanding(standing)
        val saved = academicRecordRepository.save(record)

        logger.info("Updated academic standing for student $studentId: $standing")

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved)

        return saved
    }
}
