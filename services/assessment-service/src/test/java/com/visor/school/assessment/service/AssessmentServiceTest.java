package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentStatus;
import com.visor.school.assessment.model.AssessmentType;
import com.visor.school.assessment.repository.AssessmentRepository;
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
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @InjectMocks
    private AssessmentService assessmentService;

    private UUID testClassId;
    private UUID testTeacherId;

    @BeforeEach
    void setup() {
        testClassId = UUID.randomUUID();
        testTeacherId = UUID.randomUUID();
        assessmentService = new AssessmentService(assessmentRepository);
    }

    @Test
    void shouldCreateAssessment() {
        // Given
        Assessment assessment = new Assessment(
            testClassId,
            "Midterm Exam",
            AssessmentType.EXAM,
            new BigDecimal("100.0"),
            testTeacherId
        );
        when(assessmentRepository.save(any(Assessment.class))).thenReturn(assessment);

        // When
        Assessment result = assessmentService.createAssessment(
            testClassId,
            "Midterm Exam",
            AssessmentType.EXAM,
            new BigDecimal("100.0"),
            testTeacherId,
            null,
            null,
            null
        );

        // Then
        assertNotNull(result);
        assertEquals("Midterm Exam", result.getName());
        assertEquals(AssessmentStatus.DRAFT, result.getStatus());
        verify(assessmentRepository).save(any(Assessment.class));
    }

    @Test
    void shouldGetAssessmentById() {
        // Given
        UUID assessmentId = UUID.randomUUID();
        Assessment assessment = new Assessment(
            testClassId,
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            testTeacherId
        );
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        // When
        Assessment result = assessmentService.getAssessment(assessmentId);

        // Then
        assertNotNull(result);
        verify(assessmentRepository).findById(assessmentId);
    }

    @Test
    void shouldReturnNullWhenAssessmentNotFound() {
        // Given
        UUID assessmentId = UUID.randomUUID();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        // When
        Assessment result = assessmentService.getAssessment(assessmentId);

        // Then
        assertNull(result);
    }

    @Test
    void shouldPublishAssessment() {
        // Given
        UUID assessmentId = UUID.randomUUID();
        Assessment assessment = new Assessment(
            testClassId,
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            testTeacherId
        );
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Assessment result = assessmentService.publishAssessment(assessmentId);

        // Then
        assertEquals(AssessmentStatus.PUBLISHED, result.getStatus());
        verify(assessmentRepository).save(assessment);
    }

    @Test
    void shouldGetAssessmentsByClassId() {
        // Given
        Assessment assessment1 = new Assessment(
            testClassId,
            "Test 1",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            testTeacherId
        );
        Assessment assessment2 = new Assessment(
            testClassId,
            "Test 2",
            AssessmentType.QUIZ,
            new BigDecimal("50.0"),
            testTeacherId
        );
        when(assessmentRepository.findByClassId(testClassId)).thenReturn(Arrays.asList(assessment1, assessment2));

        // When
        List<Assessment> result = assessmentService.getAssessmentsByClass(testClassId);

        // Then
        assertEquals(2, result.size());
        verify(assessmentRepository).findByClassId(testClassId);
    }
}
