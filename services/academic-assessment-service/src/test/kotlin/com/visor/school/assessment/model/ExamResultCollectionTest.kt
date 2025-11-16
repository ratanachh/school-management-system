package com.visor.school.assessment.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ExamResultCollectionTest {

    @Test
    fun `should create exam result collection with required fields`() {
        val collection = ExamResultCollection(
            classId = UUID.randomUUID(),
            collectedBy = UUID.randomUUID(),
            academicYear = "2024-2025",
            term = "FIRST_TERM"
        )

        assertNotNull(collection.id)
        assertNotNull(collection.classId)
        assertNotNull(collection.collectedBy)
        assertEquals("2024-2025", collection.academicYear)
        assertEquals("FIRST_TERM", collection.term)
        assertEquals(ExamResultCollectionStatus.COLLECTING, collection.status)
        assertNotNull(collection.collectedAt)
    }

    @Test
    fun `should create exam result collection with all fields`() {
        val collection = ExamResultCollection(
            classId = UUID.randomUUID(),
            collectedBy = UUID.randomUUID(),
            academicYear = "2024-2025",
            term = "FIRST_TERM",
            status = ExamResultCollectionStatus.COMPLETED,
            summary = "All exam results collected successfully",
            metadata = mapOf("subjectCount" to "5", "studentCount" to "30")
        )

        assertEquals(ExamResultCollectionStatus.COMPLETED, collection.status)
        assertEquals("All exam results collected successfully", collection.summary)
        assertNotNull(collection.metadata)
    }

    @Test
    fun `should accept all collection status types`() {
        val statuses = listOf(
            ExamResultCollectionStatus.COLLECTING,
            ExamResultCollectionStatus.COMPLETED,
            ExamResultCollectionStatus.SUBMITTED
        )

        statuses.forEach { status ->
            val collection = ExamResultCollection(
                classId = UUID.randomUUID(),
                collectedBy = UUID.randomUUID(),
                academicYear = "2024-2025",
                term = "FIRST_TERM",
                status = status
            )

            assertEquals(status, collection.status)
        }
    }

    @Test
    fun `should accept all term types`() {
        val terms = listOf("FIRST_TERM", "SECOND_TERM", "THIRD_TERM", "FULL_YEAR")

        terms.forEach { term ->
            val collection = ExamResultCollection(
                classId = UUID.randomUUID(),
                collectedBy = UUID.randomUUID(),
                academicYear = "2024-2025",
                term = term
            )

            assertEquals(term, collection.term)
        }
    }
}

