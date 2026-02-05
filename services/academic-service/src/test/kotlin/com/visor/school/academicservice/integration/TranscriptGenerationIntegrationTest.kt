package com.visor.school.academicservice.integration

import com.visor.school.academicservice.config.TestConfig
import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.repository.AcademicRecordRepository
import com.visor.school.academicservice.service.AcademicRecordService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Integration test for transcript generation
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig::class)
@Transactional
class TranscriptGenerationIntegrationTest @Autowired constructor(
    private val academicRecordService: AcademicRecordService,
    private val academicRecordRepository: AcademicRecordRepository
) {

    @Test
    fun `should generate transcript for student with academic history`() {
        // Given
        val studentId = UUID.randomUUID()
        val record = AcademicRecord(
            studentId = studentId,
            currentGPA = BigDecimal("3.5"),
            cumulativeGPA = BigDecimal("3.5"),
            creditsEarned = 0,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        val entry1 = EnrollmentEntry(
            academicYear = "2023-2024",
            term = Term.FIRST_TERM,
            gradeLevel = 9,
            enrollmentDate = LocalDate.of(2023, 9, 1),
            status = EnrollmentStatus.ENROLLED
        )
        val entry2 = EnrollmentEntry(
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            gradeLevel = 10,
            enrollmentDate = LocalDate.of(2024, 9, 1),
            status = EnrollmentStatus.ENROLLED
        )

        val course1 = CourseCompletion(
            courseName = "Mathematics 101",
            subject = "Mathematics",
            gradeLevel = 9,
            finalGrade = "A",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )
        val course2 = CourseCompletion(
            courseName = "English 101",
            subject = "English",
            gradeLevel = 9,
            finalGrade = "B",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )

        record.addEnrollmentEntry(entry1)
        record.addEnrollmentEntry(entry2)
        record.addCompletedCourse(course1)
        record.addCompletedCourse(course2)

        academicRecordRepository.save(record)

        // When
        val transcript = academicRecordService.generateTranscript(studentId)

        // Then
        assertNotNull(transcript)
        assertTrue(transcript.isNotEmpty())
    }

    @Test
    fun `should generate transcript for graduated student`() {
        // Given
        val studentId = UUID.randomUUID()
        val record = AcademicRecord(
            studentId = studentId,
            currentGPA = BigDecimal("3.8"),
            cumulativeGPA = BigDecimal("3.7"),
            creditsEarned = 120,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        record.markAsGraduated(LocalDate.of(2025, 6, 15))
        academicRecordRepository.save(record)

        // When
        val transcript = academicRecordService.generateTranscript(studentId)

        // Then
        assertNotNull(transcript)
        assertTrue(transcript.isNotEmpty())
    }

    @Test
    fun `should handle transcript generation for student without completed courses`() {
        // Given
        val studentId = UUID.randomUUID()
        val record = AcademicRecord(
            studentId = studentId,
            currentGPA = BigDecimal.ZERO,
            cumulativeGPA = BigDecimal.ZERO,
            creditsEarned = 0,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        academicRecordRepository.save(record)

        // When
        val transcript = academicRecordService.generateTranscript(studentId)

        // Then
        assertNotNull(transcript)
        // Transcript should still be generated even with no courses
    }
}

