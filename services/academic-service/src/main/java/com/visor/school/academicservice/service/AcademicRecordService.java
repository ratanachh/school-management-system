package com.visor.school.academicservice.service;

import com.visor.school.academicservice.event.AcademicRecordEventPublisher;
import com.visor.school.academicservice.model.AcademicRecord;
import com.visor.school.academicservice.model.AcademicStanding;
import com.visor.school.academicservice.repository.AcademicRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Academic record service for managing student academic history
 */
@Service
@Transactional
public class AcademicRecordService {
    private static final Logger logger = LoggerFactory.getLogger(AcademicRecordService.class);

    private final AcademicRecordRepository academicRecordRepository;
    private final GPACalculator gpaCalculator;
    private final TranscriptGenerator transcriptGenerator;
    private final AcademicRecordEventPublisher academicRecordEventPublisher;

    public AcademicRecordService(
            AcademicRecordRepository academicRecordRepository,
            GPACalculator gpaCalculator,
            TranscriptGenerator transcriptGenerator,
            AcademicRecordEventPublisher academicRecordEventPublisher
    ) {
        this.academicRecordRepository = academicRecordRepository;
        this.gpaCalculator = gpaCalculator;
        this.transcriptGenerator = transcriptGenerator;
        this.academicRecordEventPublisher = academicRecordEventPublisher;
    }

    /**
     * Get academic record by student ID
     */
    @Transactional(readOnly = true)
    public AcademicRecord getAcademicRecord(UUID studentId) {
        return academicRecordRepository.findByStudentId(studentId).orElse(null);
    }

    /**
     * Get or create academic record for a student
     */
    public AcademicRecord getOrCreateAcademicRecord(UUID studentId) {
        Optional<AcademicRecord> existing = academicRecordRepository.findByStudentId(studentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        AcademicRecord newRecord = new AcademicRecord(
                studentId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                120,
                AcademicStanding.GOOD_STANDING
        );

        AcademicRecord saved = academicRecordRepository.save(newRecord);
        logger.info("Created academic record for student: {}", studentId);
        return saved;
    }

    /**
     * Generate transcript PDF for a student
     */
    @Transactional(readOnly = true)
    public byte[] generateTranscript(UUID studentId) {
        AcademicRecord record = academicRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Academic record not found for student: " + studentId));

        logger.info("Generating transcript for student: {}", studentId);
        return transcriptGenerator.generate(record);
    }

    /**
     * Update GPA for a student's academic record
     */
    public AcademicRecord updateGPA(
            UUID studentId,
            BigDecimal currentGPA,
            BigDecimal cumulativeGPA
    ) {
        AcademicRecord record = academicRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Academic record not found for student: " + studentId));

        record.updateGPA(currentGPA, cumulativeGPA);
        AcademicRecord saved = academicRecordRepository.save(record);

        logger.info("Updated GPA for student {}: current={}, cumulative={}", studentId, currentGPA, cumulativeGPA);

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved);

        return saved;
    }

    /**
     * Recalculate GPA from completed courses
     */
    public AcademicRecord recalculateGPA(UUID studentId) {
        AcademicRecord record = academicRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Academic record not found for student: " + studentId));

        BigDecimal calculatedGPA = gpaCalculator.calculateGPA(record.getCompletedCourses());
        record.updateGPA(calculatedGPA, calculatedGPA);

        AcademicRecord saved = academicRecordRepository.save(record);
        logger.info("Recalculated GPA for student {}: {}", studentId, calculatedGPA);

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved);

        return saved;
    }

    /**
     * Update academic standing
     */
    public AcademicRecord updateAcademicStanding(UUID studentId, AcademicStanding standing) {
        AcademicRecord record = academicRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Academic record not found for student: " + studentId));

        record.updateAcademicStanding(standing);
        AcademicRecord saved = academicRecordRepository.save(record);

        logger.info("Updated academic standing for student {}: {}", studentId, standing);

        // Publish event
        academicRecordEventPublisher.publishAcademicRecordUpdated(saved);

        return saved;
    }
}
