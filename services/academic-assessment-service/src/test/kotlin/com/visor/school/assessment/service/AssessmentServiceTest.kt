package com.visor.school.assessment.service

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentStatus
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.repository.AssessmentRepository
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
class AssessmentServiceTest {

    @Mock
    private lateinit var assessmentRepository: AssessmentRepository

    @InjectMocks
    private lateinit var assessmentService: AssessmentService

    private val testClassId = UUID.randomUUID()
    private val testTeacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        assessmentService = AssessmentService(assessmentRepository)
    }

    @Test
    fun `should create assessment`() {
        // Given
        val assessment = Assessment(
            classId = testClassId,
            name = "Midterm Exam",
            type = AssessmentType.EXAM,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        whenever(assessmentRepository.save(any())).thenReturn(assessment)

        // When
        val result = assessmentService.createAssessment(
            classId = testClassId,
            name = "Midterm Exam",
            type = AssessmentType.EXAM,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )

        // Then
        assertNotNull(result)
        assertEquals("Midterm Exam", result.name)
        assertEquals(AssessmentStatus.DRAFT, result.status)
        verify(assessmentRepository).save(any())
    }

    @Test
    fun `should get assessment by id`() {
        // Given
        val assessmentId = UUID.randomUUID()
        val assessment = Assessment(
            id = assessmentId,
            classId = testClassId,
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        whenever(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment))

        // When
        val result = assessmentService.getAssessment(assessmentId)

        // Then
        assertNotNull(result)
        assertEquals(assessmentId, result?.id)
        verify(assessmentRepository).findById(assessmentId)
    }

    @Test
    fun `should return null when assessment not found`() {
        // Given
        val assessmentId = UUID.randomUUID()
        whenever(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty())

        // When
        val result = assessmentService.getAssessment(assessmentId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should publish assessment`() {
        // Given
        val assessmentId = UUID.randomUUID()
        val assessment = Assessment(
            id = assessmentId,
            classId = testClassId,
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        whenever(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment))
        whenever(assessmentRepository.save(any())).thenAnswer { it.arguments[0] as Assessment }

        // When
        val result = assessmentService.publishAssessment(assessmentId)

        // Then
        assertEquals(AssessmentStatus.PUBLISHED, result.status)
        verify(assessmentRepository).save(assessment)
    }

    @Test
    fun `should get assessments by class id`() {
        // Given
        val assessment1 = Assessment(
            classId = testClassId,
            name = "Test 1",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        val assessment2 = Assessment(
            classId = testClassId,
            name = "Test 2",
            type = AssessmentType.QUIZ,
            totalPoints = BigDecimal("50.0"),
            createdBy = testTeacherId
        )
        whenever(assessmentRepository.findByClassId(testClassId)).thenReturn(listOf(assessment1, assessment2))

        // When
        val result = assessmentService.getAssessmentsByClass(testClassId)

        // Then
        assertEquals(2, result.size)
        verify(assessmentRepository).findByClassId(testClassId)
    }
}

