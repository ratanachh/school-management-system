package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class ClassRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val classRepository: ClassRepository
) {

    private lateinit var testHomeroomClass: Class
    private lateinit var testSubjectClass: Class

    @BeforeEach
    fun setup() {
        testHomeroomClass = Class(
            className = "Grade 3 Homeroom",
            classType = ClassType.HOMEROOM,
            subject = null,
            gradeLevel = 3,
            homeroomTeacherId = UUID.randomUUID(),
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )
        entityManager.persistAndFlush(testHomeroomClass)

        testSubjectClass = Class(
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )
        entityManager.persistAndFlush(testSubjectClass)
    }

    @Test
    fun `should find class by className`() {
        val found = classRepository.findByClassName("Grade 3 Homeroom")

        assertTrue(found.isNotEmpty())
        assertTrue(found.any { it.className == "Grade 3 Homeroom" })
    }

    @Test
    fun `should find classes by grade level`() {
        val grade3Classes = classRepository.findByGradeLevel(3)
        val grade10Classes = classRepository.findByGradeLevel(10)

        assertTrue(grade3Classes.isNotEmpty())
        assertTrue(grade3Classes.all { it.gradeLevel == 3 })
        assertTrue(grade10Classes.isNotEmpty())
        assertTrue(grade10Classes.all { it.gradeLevel == 10 })
    }

    @Test
    fun `should find classes by class type`() {
        val homeroomClasses = classRepository.findByClassType(ClassType.HOMEROOM)
        val subjectClasses = classRepository.findByClassType(ClassType.SUBJECT)

        assertTrue(homeroomClasses.isNotEmpty())
        assertTrue(homeroomClasses.all { it.classType == ClassType.HOMEROOM })
        assertTrue(subjectClasses.isNotEmpty())
        assertTrue(subjectClasses.all { it.classType == ClassType.SUBJECT })
    }

    @Test
    fun `should find classes by academic year and term`() {
        val classes = classRepository.findByAcademicYearAndTerm("2024-2025", Term.FIRST_TERM)

        assertTrue(classes.isNotEmpty())
        assertTrue(classes.all { it.academicYear == "2024-2025" && it.term == Term.FIRST_TERM })
    }

    @Test
    fun `should find homeroom classes by teacher`() {
        val homeroomTeacherId = testHomeroomClass.homeroomTeacherId!!
        val classes = classRepository.findByHomeroomTeacherId(homeroomTeacherId)

        assertTrue(classes.isNotEmpty())
        assertTrue(classes.all { it.homeroomTeacherId == homeroomTeacherId })
    }

    @Test
    fun `should find subject classes by class teacher`() {
        val classTeacherId = UUID.randomUUID()
        val classWithTeacher = Class(
            className = "English 101",
            classType = ClassType.SUBJECT,
            subject = "English",
            gradeLevel = 11,
            homeroomTeacherId = null,
            classTeacherId = classTeacherId,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )
        entityManager.persistAndFlush(classWithTeacher)

        val classes = classRepository.findByClassTeacherId(classTeacherId)

        assertTrue(classes.isNotEmpty())
        assertTrue(classes.all { it.classTeacherId == classTeacherId })
    }

    @Test
    fun `should find classes by status`() {
        val inProgressClass = Class(
            className = "In Progress Class",
            classType = ClassType.SUBJECT,
            subject = "Science",
            gradeLevel = 8,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.IN_PROGRESS
        )
        entityManager.persistAndFlush(inProgressClass)

        val inProgressClasses = classRepository.findByStatus(ClassStatus.IN_PROGRESS)

        assertTrue(inProgressClasses.isNotEmpty())
        assertTrue(inProgressClasses.all { it.status == ClassStatus.IN_PROGRESS })
    }
}

