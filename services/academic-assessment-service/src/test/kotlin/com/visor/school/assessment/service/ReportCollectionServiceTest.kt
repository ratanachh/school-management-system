package com.visor.school.assessment.service

import com.visor.school.assessment.model.*
import com.visor.school.assessment.repository.ExamResultCollectionRepository
import com.visor.school.assessment.repository.GradeRepository
import com.visor.school.assessment.repository.ReportSubmissionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReportCollectionServiceTest {

    @Mock
    private lateinit var examResultCollectionRepository: ExamResultCollectionRepository

    @Mock
    private lateinit var reportSubmissionRepository: ReportSubmissionRepository

    @Mock
    private lateinit var gradeRepository: GradeRepository

    @InjectMocks
    private lateinit var reportCollectionService: ReportCollectionService

    private val testClassId = UUID.randomUUID()
    private val testClassTeacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        reportCollectionService = ReportCollectionService(
            examResultCollectionRepository,
            reportSubmissionRepository,
            gradeRepository
        )
    }

    @Test
    fun `should collect exam results for a class`() {
        // Given
        val collection = ExamResultCollection(
            classId = testClassId,
            collectedBy = testClassTeacherId,
            academicYear = "2024-2025",
            term = "FIRST_TERM"
        )
        whenever(examResultCollectionRepository.save(any())).thenReturn(collection)
        whenever(gradeRepository.findByClassIdAndAcademicYearAndTerm(
            any(), any(), any()
        )).thenReturn(emptyList())

        // When
        val result = reportCollectionService.collectExamResults(
            classId = testClassId,
            classTeacherId = testClassTeacherId,
            academicYear = "2024-2025",
            term = "FIRST_TERM"
        )

        // Then
        assertNotNull(result)
        assertEquals(testClassId, result.classId)
        assertEquals(testClassTeacherId, result.collectedBy)
        verify(examResultCollectionRepository).save(any())
    }

    @Test
    fun `should aggregate report from collected exam results`() {
        // Given
        val collection = ExamResultCollection(
            id = UUID.randomUUID(),
            classId = testClassId,
            collectedBy = testClassTeacherId,
            academicYear = "2024-2025",
            term = "FIRST_TERM",
            status = ExamResultCollectionStatus.COMPLETED
        )
        whenever(examResultCollectionRepository.findById(collection.id))
            .thenReturn(java.util.Optional.of(collection))

        // When
        val aggregatedReport = reportCollectionService.aggregateReport(collection.id)

        // Then
        assertNotNull(aggregatedReport)
        verify(examResultCollectionRepository).findById(collection.id)
    }

    @Test
    fun `should submit report to school administration`() {
        // Given
        val collection = ExamResultCollection(
            id = UUID.randomUUID(),
            classId = testClassId,
            collectedBy = testClassTeacherId,
            academicYear = "2024-2025",
            term = "FIRST_TERM",
            status = ExamResultCollectionStatus.COMPLETED
        )
        val submission = ReportSubmission(
            collectionId = collection.id,
            submittedBy = testClassTeacherId,
            classId = testClassId,
            reportData = mapOf("summary" to "Report data")
        )
        whenever(examResultCollectionRepository.findById(collection.id))
            .thenReturn(java.util.Optional.of(collection))
        whenever(reportSubmissionRepository.save(any())).thenReturn(submission)

        // When
        val result = reportCollectionService.submitReport(
            collectionId = collection.id,
            classTeacherId = testClassTeacherId,
            reportData = mapOf("summary" to "Report data")
        )

        // Then
        assertNotNull(result)
        assertEquals(testClassId, result.classId)
        verify(reportSubmissionRepository).save(any())
    }

    @Test
    fun `should throw exception when collection not found`() {
        // Given
        val collectionId = UUID.randomUUID()
        whenever(examResultCollectionRepository.findById(collectionId))
            .thenReturn(java.util.Optional.empty())

        // When & Then
        assertThrows(NoSuchElementException::class.java) {
            reportCollectionService.aggregateReport(collectionId)
        }
    }
}

