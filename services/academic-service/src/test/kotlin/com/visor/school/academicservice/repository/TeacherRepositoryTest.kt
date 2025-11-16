package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
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
class TeacherRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val teacherRepository: TeacherRepository
) {

    private lateinit var testTeacher: Teacher

    @BeforeEach
    fun setup() {
        testTeacher = Teacher(
            employeeId = "EMP-TEST-001",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Mathematics", "Physics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )
        entityManager.persistAndFlush(testTeacher)
    }

    @Test
    fun `should find teacher by employeeId`() {
        val found = teacherRepository.findByEmployeeId("EMP-TEST-001")

        assertTrue(found.isPresent)
        assertEquals("EMP-TEST-001", found.get().employeeId)
        assertEquals(2, found.get().subjectSpecializations.size)
    }

    @Test
    fun `should find teacher by userId`() {
        val found = teacherRepository.findByUserId(testTeacher.userId)

        assertTrue(found.isPresent)
        assertEquals(testTeacher.userId, found.get().userId)
        assertEquals("EMP-TEST-001", found.get().employeeId)
    }

    @Test
    fun `should find teachers by employment status`() {
        val onLeaveTeacher = Teacher(
            employeeId = "EMP-TEST-002",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("English"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ON_LEAVE
        )
        entityManager.persistAndFlush(onLeaveTeacher)

        val activeTeachers = teacherRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE)
        val onLeaveTeachers = teacherRepository.findByEmploymentStatus(EmploymentStatus.ON_LEAVE)

        assertTrue(activeTeachers.isNotEmpty())
        assertTrue(activeTeachers.all { it.employmentStatus == EmploymentStatus.ACTIVE })
        assertTrue(onLeaveTeachers.isNotEmpty())
        assertTrue(onLeaveTeachers.all { it.employmentStatus == EmploymentStatus.ON_LEAVE })
    }

    @Test
    fun `should find teachers by department`() {
        val scienceTeacher = Teacher(
            employeeId = "EMP-TEST-003",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Chemistry"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = "Science"
        )
        entityManager.persistAndFlush(scienceTeacher)

        val scienceTeachers = teacherRepository.findByDepartment("Science")

        assertTrue(scienceTeachers.isNotEmpty())
        assertTrue(scienceTeachers.all { it.department == "Science" })
    }

    @Test
    fun `should return empty when teacher not found by employeeId`() {
        val found = teacherRepository.findByEmployeeId("NONEXISTENT")

        assertFalse(found.isPresent)
    }

    @Test
    fun `should return empty when teacher not found by userId`() {
        val found = teacherRepository.findByUserId(UUID.randomUUID())

        assertFalse(found.isPresent)
    }
}

