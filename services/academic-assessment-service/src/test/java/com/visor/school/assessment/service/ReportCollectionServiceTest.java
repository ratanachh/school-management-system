package com.visor.school.assessment.service;

import com.visor.school.assessment.event.ReportEventPublisher;
import com.visor.school.assessment.model.ExamResultCollection;
import com.visor.school.assessment.model.ExamResultCollectionStatus;
import com.visor.school.assessment.model.ReportSubmission;
import com.visor.school.assessment.repository.ExamResultCollectionRepository;
import com.visor.school.assessment.repository.GradeRepository;
import com.visor.school.assessment.repository.ReportSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportCollectionServiceTest {

    @Mock
    private ExamResultCollectionRepository examResultCollectionRepository;

    @Mock
    private ReportSubmissionRepository reportSubmissionRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ReportEventPublisher reportEventPublisher;

    @InjectMocks
    private ReportCollectionService reportCollectionService;

    private UUID testClassId;
    private UUID testClassTeacherId;

    @BeforeEach
    void setup() {
        testClassId = UUID.randomUUID();
        testClassTeacherId = UUID.randomUUID();
        reportCollectionService = new ReportCollectionService(
            examResultCollectionRepository,
            reportSubmissionRepository,
            gradeRepository,
            reportEventPublisher
        );
    }

    @Test
    void shouldCollectExamResultsForAClass() {
        // Given
        ExamResultCollection collection = new ExamResultCollection(
            testClassId,
            testClassTeacherId,
            "2024-2025",
            "FIRST_TERM"
        );
        when(examResultCollectionRepository.save(any(ExamResultCollection.class))).thenReturn(collection);

        // When
        ExamResultCollection result = reportCollectionService.collectExamResults(
            testClassId,
            testClassTeacherId,
            "2024-2025",
            "FIRST_TERM"
        );

        // Then
        assertNotNull(result);
        assertEquals(testClassId, result.getClassId());
        assertEquals(testClassTeacherId, result.getCollectedBy());
        verify(examResultCollectionRepository).save(any(ExamResultCollection.class));
    }

    @Test
    void shouldAggregateReportFromCollectedExamResults() {
        // Given
        UUID collectionId = UUID.randomUUID();
        ExamResultCollection collection = new ExamResultCollection(
            testClassId,
            testClassTeacherId,
            "2024-2025",
            "FIRST_TERM"
        );
        collection.setStatus(ExamResultCollectionStatus.COMPLETED);
        when(examResultCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        // When
        Map<String, Object> aggregatedReport = reportCollectionService.aggregateReport(collectionId);

        // Then
        assertNotNull(aggregatedReport);
        verify(examResultCollectionRepository).findById(collectionId);
    }

    @Test
    void shouldSubmitReportToSchoolAdministration() {
        // Given
        UUID collectionId = UUID.randomUUID();
        ExamResultCollection collection = new ExamResultCollection(
            testClassId,
            testClassTeacherId,
            "2024-2025",
            "FIRST_TERM"
        );
        collection.setStatus(ExamResultCollectionStatus.COMPLETED);
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("summary", "Report data");

        ReportSubmission submission = new ReportSubmission(
            collectionId,
            testClassTeacherId,
            testClassId,
            reportData
        );

        when(examResultCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(reportSubmissionRepository.save(any(ReportSubmission.class))).thenReturn(submission);

        // When
        ReportSubmission result = reportCollectionService.submitReport(
            collectionId,
            testClassTeacherId,
            reportData
        );

        // Then
        assertNotNull(result);
        assertEquals(testClassId, result.getClassId());
        verify(reportSubmissionRepository).save(any(ReportSubmission.class));
    }

    @Test
    void shouldThrowExceptionWhenCollectionNotFound() {
        // Given
        UUID collectionId = UUID.randomUUID();
        when(examResultCollectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> {
            reportCollectionService.aggregateReport(collectionId);
        });
    }
}
