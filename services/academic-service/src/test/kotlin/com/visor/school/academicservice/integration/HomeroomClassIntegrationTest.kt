package com.visor.school.academicservice.integration

import com.visor.school.academicservice.config.TestConfig
import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.repository.ClassRepository
import com.visor.school.academicservice.repository.TeacherRepository
import com.visor.school.academicservice.service.ClassService
import com.visor.school.academicservice.service.TeacherService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import org.junit.jupiter.api.assertThrows

/**
 * Integration test for homeroom class creation (grades 1-6)
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig::class)
@Transactional
class HomeroomClassIntegrationTest @Autowired constructor(
    private val teacherService: TeacherService,
    private val classService: ClassService,
    private val teacherRepository: TeacherRepository,
    private val classRepository: ClassRepository
) {

    @Test
    fun `should create homeroom class for grade 1`() {
        // Given - Create teacher
        val userId = UUID.randomUUID()
        val teacher = teacherService.createTeacher(
            userId = userId,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        // When
        val homeroomClass = classService.createHomeroomClass(
            className = "Grade 1 Homeroom",
            gradeLevel = 1,
            homeroomTeacherId = teacher.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // Then
        assertNotNull(homeroomClass)
        assertEquals(ClassType.HOMEROOM, homeroomClass.classType)
        assertEquals(1, homeroomClass.gradeLevel)
        assertEquals(teacher.id!!, homeroomClass.homeroomTeacherId)
        assertNull(homeroomClass.subject)
        assertNull(homeroomClass.classTeacherId)

        // Verify persisted
        val saved = classRepository.findById(homeroomClass.id!!)
        assertTrue(saved.isPresent)
        assertEquals(ClassType.HOMEROOM, saved.get().classType)
    }

    @Test
    fun `should create homeroom class for grade 6`() {
        // Given - Create teacher
        val userId = UUID.randomUUID()
        val teacher = teacherService.createTeacher(
            userId = userId,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        // When
        val homeroomClass = classService.createHomeroomClass(
            className = "Grade 6 Homeroom",
            gradeLevel = 6,
            homeroomTeacherId = teacher.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // Then
        assertEquals(6, homeroomClass.gradeLevel)
        assertEquals(ClassType.HOMEROOM, homeroomClass.classType)
    }

    @Test
    fun `should fail to create homeroom class for grade 7`() {
        // Given - Create teacher
        val userId = UUID.randomUUID()
        val teacher = teacherService.createTeacher(
            userId = userId,
            qualifications = listOf("Teaching Certificate"),
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            classService.createHomeroomClass(
                className = "Invalid Homeroom",
                gradeLevel = 7, // Invalid: homeroom only for grades 1-6
                homeroomTeacherId = teacher.id!!,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should enforce one homeroom class per grade per academic year`() {
        // Given - Create teacher and first homeroom class
        val userId = UUID.randomUUID()
        val teacher = teacherService.createTeacher(
            userId = userId,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        classService.createHomeroomClass(
            className = "Grade 3 Homeroom A",
            gradeLevel = 3,
            homeroomTeacherId = teacher.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // When & Then - Try to create duplicate homeroom class
        assertThrows<IllegalArgumentException> {
            classService.createHomeroomClass(
                className = "Grade 3 Homeroom B",
                gradeLevel = 3,
                homeroomTeacherId = teacher.id!!,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should allow different homeroom classes for different grades`() {
        // Given - Create two teachers
        val userId1 = UUID.randomUUID()
        val teacher1 = teacherService.createTeacher(
            userId = userId1,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        val userId2 = UUID.randomUUID()
        val teacher2 = teacherService.createTeacher(
            userId = userId2,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        // When
        val grade3Class = classService.createHomeroomClass(
            className = "Grade 3 Homeroom",
            gradeLevel = 3,
            homeroomTeacherId = teacher1.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        val grade4Class = classService.createHomeroomClass(
            className = "Grade 4 Homeroom",
            gradeLevel = 4,
            homeroomTeacherId = teacher2.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // Then
        assertNotNull(grade3Class)
        assertNotNull(grade4Class)
        assertEquals(3, grade3Class.gradeLevel)
        assertEquals(4, grade4Class.gradeLevel)
        assertNotEquals(grade3Class.id!!, grade4Class.id!!)
    }

    @Test
    fun `should allow homeroom classes for different terms`() {
        // Given - Create teacher
        val userId = UUID.randomUUID()
        val teacher = teacherService.createTeacher(
            userId = userId,
            qualifications = listOf("Elementary Education Certificate"),
            subjectSpecializations = listOf("General Education"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = ""
        )

        // When - Create homeroom classes for different terms
        val firstTermClass = classService.createHomeroomClass(
            className = "Grade 3 Homeroom - Term 1",
            gradeLevel = 3,
            homeroomTeacherId = teacher.id!!,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        val secondTermClass = classService.createHomeroomClass(
            className = "Grade 3 Homeroom - Term 2",
            gradeLevel = 3,
            homeroomTeacherId = teacher.id!!,
            academicYear = "2024-2025",
            term = Term.SECOND_TERM,
            startDate = LocalDate.of(2025, 1, 1)
        )

        // Then
        assertNotNull(firstTermClass)
        assertNotNull(secondTermClass)
        assertEquals(Term.FIRST_TERM, firstTermClass.term)
        assertEquals(Term.SECOND_TERM, secondTermClass.term)
    }
}
