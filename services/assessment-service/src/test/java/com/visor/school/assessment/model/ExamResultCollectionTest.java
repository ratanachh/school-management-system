package com.visor.school.assessment.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExamResultCollectionTest {

    @Test
    void shouldCreateExamResultCollectionWithRequiredFields() {
        ExamResultCollection collection = new ExamResultCollection(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "2024-2025",
            "FIRST_TERM"
        );

        assertNotNull(collection.getId());
        assertNotNull(collection.getClassId());
        assertNotNull(collection.getCollectedBy());
        assertEquals("2024-2025", collection.getAcademicYear());
        assertEquals("FIRST_TERM", collection.getTerm());
        assertEquals(ExamResultCollectionStatus.COLLECTING, collection.getStatus());
        assertNotNull(collection.getCollectedAt());
    }

    @Test
    void shouldCreateExamResultCollectionWithAllFields() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("subjectCount", "5");
        metadata.put("studentCount", "30");

        ExamResultCollection collection = new ExamResultCollection(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "2024-2025",
            "FIRST_TERM",
            metadata
        );
        collection.setStatus(ExamResultCollectionStatus.COMPLETED);
        collection.setSummary("All exam results collected successfully");

        assertEquals(ExamResultCollectionStatus.COMPLETED, collection.getStatus());
        assertEquals("All exam results collected successfully", collection.getSummary());
        assertNotNull(collection.getMetadata());
    }

    @Test
    void shouldAcceptAllCollectionStatusTypes() {
        List<ExamResultCollectionStatus> statuses = Arrays.asList(
            ExamResultCollectionStatus.COLLECTING,
            ExamResultCollectionStatus.COMPLETED,
            ExamResultCollectionStatus.SUBMITTED
        );

        for (ExamResultCollectionStatus status : statuses) {
            ExamResultCollection collection = new ExamResultCollection(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "2024-2025",
                "FIRST_TERM"
            );
            collection.setStatus(status);

            assertEquals(status, collection.getStatus());
        }
    }

    @Test
    void shouldAcceptAllTermTypes() {
        List<String> terms = Arrays.asList("FIRST_TERM", "SECOND_TERM", "THIRD_TERM", "FULL_YEAR");

        for (String term : terms) {
            ExamResultCollection collection = new ExamResultCollection(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "2024-2025",
                term
            );

            assertEquals(term, collection.getTerm());
        }
    }
}
