package com.visor.school.assessment.service;

import com.visor.school.assessment.event.GradeEventPublisher;
import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentType;
import com.visor.school.assessment.model.Grade;
import com.visor.school.assessment.repository.AssessmentRepository;
import com.visor.school.assessment.repository.GradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private GradeCalculator gradeCalculator;

    @Mock
    private LetterGradeConverter letterGradeConverter;

    @Mock
    private GradeEventPublisher gradeEventPublisher;

    @InjectMocks
    private GradeService gradeService;

    private UUID testStudentId;
    private UUID testAssessmentId;
    private UUID testTeacherId;

    @BeforeEach
    void setup() {
        testStudentId = UUID.randomUUID();
        testAssessmentId = UUID.randomUUID();
        testTeacherId = UUID.randomUUID();
        gradeService = new GradeService(
            gradeRepository,
            assessmentRepository,
            gradeCalculator,
            letterGradeConverter,
            gradeEventPublisher
        );
    }

    @Test
    void shouldRecordGrade() {
        // Given
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            testTeacherId
        );
        assessment.publish();

        when(assessmentRepository.findById(testAssessmentId)).thenReturn(Optional.of(assessment));
        when(gradeRepository.findByStudentIdAndAssessmentId(testStudentId, testAssessmentId))
            .thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(letterGradeConverter.convert(any(BigDecimal.class))).thenReturn("B");

        // When
        Grade result = gradeService.recordGrade(
            testStudentId,
            testAssessmentId,
            new BigDecimal("85.0"),
            testTeacherId,
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("85.0").compareTo(result.getScore()));
        assertEquals(0, new BigDecimal("85.0").compareTo(result.getPercentage()));
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void shouldThrowExceptionWhenAssessmentNotPublished() {
        // Given
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            testTeacherId
        );
        // Assessment is in DRAFT status
        when(assessmentRepository.findById(testAssessmentId)).thenReturn(Optional.of(assessment));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.recordGrade(
                testStudentId,
                testAssessmentId,
                new BigDecimal("85.0"),
                testTeacherId,
                null
            );
        });
    }

    @Test
    void shouldUpdateExistingGrade() {
        // Given
        Grade existingGrade = new Grade(
            testStudentId,
            testAssessmentId,
            new BigDecimal("80.0"),
            new BigDecimal("100.0"),
            testTeacherId
        );

        when(gradeRepository.findByStudentIdAndAssessmentId(testStudentId, testAssessmentId))
            .thenReturn(Optional.of(existingGrade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(letterGradeConverter.convert(any(BigDecimal.class))).thenReturn("A");

        // When
        Grade result = gradeService.updateGrade(
            testStudentId,
            testAssessmentId,
            new BigDecimal("95.0"),
            testTeacherId,
            null
        );

        // Then
        assertEquals(0, new BigDecimal("95.0").compareTo(result.getScore()));
        assertEquals(testTeacherId, result.getUpdatedBy());
        verify(gradeRepository).save(existingGrade);
    }

    @Test
    void shouldCalculateAverageGrade() {
        // Given
        List<Grade> grades = Arrays.asList(
            new Grade(
                testStudentId,
                UUID.randomUUID(),
                new BigDecimal("85.0"),
                new BigDecimal("100.0"),
                testTeacherId
            ),
            new Grade(
                testStudentId,
                UUID.randomUUID(),
                new BigDecimal("90.0"),
                new BigDecimal("100.0"),
                testTeacherId
            )
        );
        when(gradeRepository.findByStudentId(testStudentId)).thenReturn(grades);
        when(gradeCalculator.calculateAverage(grades)).thenReturn(new BigDecimal("87.5"));

        // When
        BigDecimal average = gradeService.calculateAverageGrade(testStudentId);

        // Then
        assertEquals(0, new BigDecimal("87.5").compareTo(average));
        verify(gradeCalculator).calculateAverage(grades);
    }
}
