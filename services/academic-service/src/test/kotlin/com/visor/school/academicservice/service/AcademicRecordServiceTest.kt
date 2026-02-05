package com.visor.school.academicservice.service

import com.visor.school.academicservice.event.AcademicRecordEventPublisher
import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.academicservice.model.AcademicStanding
import com.visor.school.academicservice.model.CourseCompletion
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

    @Test
    fun `should get academic record by student ID`() {
        // Given
        val record = AcademicRecord(
            studentId = testStudentId,
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 24,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        whenever(academicRecordRepository.findByStudentId(testStudentId))
            .thenReturn(Optional.of(record))

        // When
        val result = academicRecordService.getAcademicRecord(testStudentId)

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result?.studentId)
        assertEquals(BigDecimal("3.4"), result?.cumulativeGPA)
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
        assertEquals(BigDecimal.ZERO, result.cumulativeGPA)
        verify(academicRecordRepository).save(any())
    }
}
