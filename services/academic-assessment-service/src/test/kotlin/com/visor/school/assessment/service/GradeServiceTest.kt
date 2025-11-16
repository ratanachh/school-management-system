package com.visor.school.assessment.service

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentStatus
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.repository.AssessmentRepository
import com.visor.school.assessment.repository.GradeRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class GradeServiceTest {

    @Mock
    private lateinit var gradeRepository: GradeRepository

    @Mock
    private lateinit var assessmentRepository: AssessmentRepository

    @Mock
    private lateinit var gradeCalculator: GradeCalculator

    @Mock
    private lateinit var letterGradeConverter: LetterGradeConverter

    @InjectMocks
    private lateinit var gradeService: GradeService

    private val testStudentId = UUID.randomUUID()
    private val testAssessmentId = UUID.randomUUID()
    private val testTeacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        gradeService = GradeService(
            gradeRepository,
            assessmentRepository,
            gradeCalculator,
            letterGradeConverter
        )
    }

    @Test
    fun `should record grade`() {
        // Given
        val assessment = Assessment(
            id = testAssessmentId,
            classId = UUID.randomUUID(),
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        assessment.publish()

        whenever(assessmentRepository.findById(testAssessmentId)).thenReturn(Optional.of(assessment))
        whenever(gradeRepository.findByStudentIdAndAssessmentId(testStudentId, testAssessmentId))
            .thenReturn(Optional.empty())
        whenever(gradeRepository.save(any())).thenAnswer { it.arguments[0] as Grade }
        whenever(letterGradeConverter.convert(any())).thenReturn("B")

        // When
        val result = gradeService.recordGrade(
            studentId = testStudentId,
            assessmentId = testAssessmentId,
            score = BigDecimal("85.0"),
            recordedBy = testTeacherId
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("85.0"), result.score)
        assertEquals(BigDecimal("85.0"), result.percentage)
        verify(gradeRepository).save(any())
    }

    @Test
    fun `should throw exception when assessment not published`() {
        // Given
        val assessment = Assessment(
            id = testAssessmentId,
            classId = UUID.randomUUID(),
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        // Assessment is in DRAFT status
        whenever(assessmentRepository.findById(testAssessmentId)).thenReturn(Optional.of(assessment))

        // When & Then
        assertThrows<IllegalArgumentException> {
            gradeService.recordGrade(
                studentId = testStudentId,
                assessmentId = testAssessmentId,
                score = BigDecimal("85.0"),
                recordedBy = testTeacherId
            )
        }
    }

    @Test
    fun `should update existing grade`() {
        // Given
        val existingGrade = Grade(
            id = UUID.randomUUID(),
            studentId = testStudentId,
            assessmentId = testAssessmentId,
            score = BigDecimal("80.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = testTeacherId
        )
        val assessment = Assessment(
            id = testAssessmentId,
            classId = UUID.randomUUID(),
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = testTeacherId
        )
        assessment.publish()

        whenever(assessmentRepository.findById(testAssessmentId)).thenReturn(Optional.of(assessment))
        whenever(gradeRepository.findByStudentIdAndAssessmentId(testStudentId, testAssessmentId))
            .thenReturn(Optional.of(existingGrade))
        whenever(gradeRepository.save(any())).thenAnswer { it.arguments[0] as Grade }
        whenever(letterGradeConverter.convert(any())).thenReturn("A")

        // When
        val result = gradeService.updateGrade(
            studentId = testStudentId,
            assessmentId = testAssessmentId,
            newScore = BigDecimal("95.0"),
            updatedBy = testTeacherId
        )

        // Then
        assertEquals(BigDecimal("95.0"), result.score)
        assertEquals(testTeacherId, result.updatedBy)
        verify(gradeRepository).save(existingGrade)
    }

    @Test
    fun `should calculate average grade`() {
        // Given
        val grades = listOf(
            Grade(
                studentId = testStudentId,
                assessmentId = UUID.randomUUID(),
                score = BigDecimal("85.0"),
                totalPoints = BigDecimal("100.0"),
                recordedBy = testTeacherId
            ),
            Grade(
                studentId = testStudentId,
                assessmentId = UUID.randomUUID(),
                score = BigDecimal("90.0"),
                totalPoints = BigDecimal("100.0"),
                recordedBy = testTeacherId
            )
        )
        whenever(gradeRepository.findByStudentId(testStudentId)).thenReturn(grades)
        whenever(gradeCalculator.calculateAverage(grades)).thenReturn(BigDecimal("87.5"))

        // When
        val average = gradeService.calculateAverageGrade(testStudentId)

        // Then
        assertEquals(BigDecimal("87.5"), average)
        verify(gradeCalculator).calculateAverage(grades)
    }
}

