package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.repository.AcademicRecordRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AcademicRecordServiceTest {

    @Mock
    private lateinit var academicRecordRepository: AcademicRecordRepository

    @Mock
    private lateinit var gpaCalculator: GPACalculator

    @Mock
    private lateinit var transcriptGenerator: TranscriptGenerator

    @Mock
    private lateinit var academicRecordEventPublisher: AcademicRecordEventPublisher

    @InjectMocks
    private lateinit var academicRecordService: AcademicRecordService

    private val testStudentId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        academicRecordService = AcademicRecordService(
            academicRecordRepository,
            gpaCalculator,
            transcriptGenerator,
            academicRecordEventPublisher
        )
    }

    @Test
    fun `should get academic record by student ID`() {
        // Given
        val record = AcademicRecord(
            studentId = testStudentId,
            currentGPA = BigDecimal("3.5"),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 24,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.of(record))

        // When
        val result = academicRecordService.getAcademicRecord(testStudentId)

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result?.studentId)
        assertEquals(BigDecimal("3.5"), result?.currentGPA)
        verify(academicRecordRepository).findByStudentId(testStudentId)
    }

    @Test
    fun `should return null when record not found`() {
        // Given
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.empty())

        // When
        val result = academicRecordService.getAcademicRecord(testStudentId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should generate transcript`() {
        // Given
        val record = AcademicRecord(
            studentId = testStudentId,
            currentGPA = BigDecimal("3.5"),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 24,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING,
            completedCourses = listOf(
                CourseCompletion(
                    courseName = "Mathematics 101",
                    subject = "Mathematics",
                    gradeLevel = 9,
                    finalGrade = "A",
                    credits = 3,
                    completionDate = LocalDate.of(2024, 6, 15)
                )
            )
        )
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.of(record))
        whenever(transcriptGenerator.generate(record)).thenReturn(ByteArray(100))

        // When
        val transcript = academicRecordService.generateTranscript(testStudentId)

        // Then
        assertNotNull(transcript)
        assertTrue(transcript.isNotEmpty())
        verify(transcriptGenerator).generate(record)
    }

    @Test
    fun `should throw exception when generating transcript for non-existent record`() {
        // Given
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.empty())

        // When & Then
        assertThrows<IllegalArgumentException> {
            academicRecordService.generateTranscript(testStudentId)
        }
    }

    @Test
    fun `should update GPA`() {
        // Given
        val record = AcademicRecord(
            studentId = testStudentId,
            currentGPA = BigDecimal("3.0"),
            cumulativeGPA = BigDecimal("3.0"),
            creditsEarned = 0,
            creditsRequired = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.of(record))
        whenever(academicRecordRepository.save(any())).thenAnswer { it.arguments[0] as AcademicRecord }
        doNothing().whenever(academicRecordEventPublisher).publishAcademicRecordUpdated(any())

        // When
        val result = academicRecordService.updateGPA(
            studentId = testStudentId,
            currentGPA = BigDecimal("3.5"),
            cumulativeGPA = BigDecimal("3.4")
        )

        // Then
        assertEquals(BigDecimal("3.5"), result.currentGPA)
        assertEquals(BigDecimal("3.4"), result.cumulativeGPA)
        verify(academicRecordRepository).save(record)
        verify(academicRecordEventPublisher).publishAcademicRecordUpdated(any())
    }

    @Test
    fun `should create academic record if not exists`() {
        // Given
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.empty())
        whenever(academicRecordRepository.save(any())).thenAnswer { it.arguments[0] as AcademicRecord }

        // When
        val result = academicRecordService.getOrCreateAcademicRecord(testStudentId)

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result.studentId)
        assertEquals(BigDecimal.ZERO, result.currentGPA)
        assertEquals(BigDecimal.ZERO, result.cumulativeGPA)
        verify(academicRecordRepository).save(any())
    }
}

