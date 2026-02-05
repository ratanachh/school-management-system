package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import com.visor.school.academicservice.repository.TeacherRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TeacherServiceTest {

    @Mock
    private lateinit var teacherRepository: TeacherRepository

    @Mock
    private lateinit var employeeIdGenerator: EmployeeIdGenerator

    @InjectMocks
    private lateinit var teacherService: TeacherService

    private val testTeacher = Teacher(
        userId = UUID.randomUUID(),
        employeeId = "T54321",
        qualifications = listOf("PhD in Physics"),
        subjectSpecializations = listOf("Physics", "Quantum Mechanics"),
        hireDate = LocalDate.of(2018, 8, 15),
        employmentStatus = EmploymentStatus.ACTIVE,
        department = "Science"
    )

    @Test
    fun `create teacher should save and return teacher`() {
        // Given
        val savedTeacher = Teacher(
            id = UUID.randomUUID(),
            userId = testTeacher.userId,
            employeeId = "T54321",
            qualifications = testTeacher.qualifications,
            subjectSpecializations = testTeacher.subjectSpecializations,
            hireDate = testTeacher.hireDate,
            employmentStatus = testTeacher.employmentStatus,
            department = testTeacher.department
        )
        whenever(employeeIdGenerator.generateEmployeeId()).thenReturn("T54321")
        whenever(teacherRepository.findByUserId(any())).thenReturn(Optional.empty())
        whenever(teacherRepository.save(any<Teacher>())).thenReturn(savedTeacher)

        // When
        val createdTeacher = teacherService.createTeacher(
            testTeacher.userId,
            testTeacher.qualifications,
            testTeacher.subjectSpecializations,
            testTeacher.hireDate,
            testTeacher.department
        )

        // Then
        assertNotNull(createdTeacher.id)
        assertNotNull(createdTeacher.employeeId)
        verify(teacherRepository).save(any<Teacher>())
    }

    @Test
    fun `get teacher by id should return teacher`() {
        // Given
        val teacherId = UUID.randomUUID()
        whenever(teacherRepository.findById(teacherId)).thenReturn(Optional.of(testTeacher))

        // When
        val foundTeacher = teacherService.getTeacherById(teacherId)

        // Then
        assertEquals(testTeacher, foundTeacher)
        verify(teacherRepository).findById(teacherId)
    }

    @Test
    fun `get teachers by status should return a list`() {
        // Given
        whenever(teacherRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE))
            .thenReturn(listOf(testTeacher))

        // When
        val teachers = teacherService.getTeachersByStatus(EmploymentStatus.ACTIVE)

        // Then
        assertFalse(teachers.isEmpty())
        assertEquals(1, teachers.size)
        verify(teacherRepository).findByEmploymentStatus(EmploymentStatus.ACTIVE)
    }

    @Test
    fun `get teachers by department should return a list`() {
        // Given
        whenever(teacherRepository.findByDepartment("Science"))
            .thenReturn(listOf(testTeacher))

        // When
        val teachers = teacherService.getTeachersByDepartment("Science")

        // Then
        assertFalse(teachers.isEmpty())
        assertEquals(1, teachers.size)
        verify(teacherRepository).findByDepartment("Science")
    }
}
