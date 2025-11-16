package com.visor.school.assessment.repository

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentStatus
import com.visor.school.assessment.model.AssessmentType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class AssessmentRepositoryTest @Autowired constructor(
    private val assessmentRepository: AssessmentRepository
) {

    @Test
    fun `should save and find assessment by id`() {
        // Given
        val assessment = Assessment(
            classId = UUID.randomUUID(),
            name = "Test Assessment",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )

        // When
        val saved = assessmentRepository.save(assessment)
        val found = assessmentRepository.findById(saved.id)

        // Then
        assertTrue(found.isPresent)
        assertEquals("Test Assessment", found.get().name)
    }

    @Test
    fun `should find assessments by class id`() {
        // Given
        val classId = UUID.randomUUID()
        val assessment1 = Assessment(
            classId = classId,
            name = "Test 1",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )
        val assessment2 = Assessment(
            classId = classId,
            name = "Test 2",
            type = AssessmentType.QUIZ,
            totalPoints = BigDecimal("50.0"),
            createdBy = UUID.randomUUID()
        )

        assessmentRepository.save(assessment1)
        assessmentRepository.save(assessment2)

        // When
        val found = assessmentRepository.findByClassId(classId)

        // Then
        assertEquals(2, found.size)
    }

    @Test
    fun `should find assessments by status`() {
        // Given
        val assessment1 = Assessment(
            classId = UUID.randomUUID(),
            name = "Draft Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )
        val assessment2 = Assessment(
            classId = UUID.randomUUID(),
            name = "Published Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )
        assessment2.publish()

        assessmentRepository.save(assessment1)
        assessmentRepository.save(assessment2)

        // When
        val published = assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED)

        // Then
        assertTrue(published.any { it.name == "Published Test" })
        assertFalse(published.any { it.name == "Draft Test" })
    }
}

