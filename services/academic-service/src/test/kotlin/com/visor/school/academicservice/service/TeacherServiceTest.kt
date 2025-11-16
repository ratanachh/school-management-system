package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import com.visor.school.academicservice.repository.TeacherRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
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

    private val testUserId = UUID.randomUUID()
    private val testEmployeeId = "EMP-2025-001"

    @BeforeEach
    fun setup() {
        teacherService = TeacherService(teacherRepository, employeeIdGenerator)
    }

    @Test
    fun `should create teacher successfully`() {
        // Given
        whenever(employeeIdGenerator.generateEmployeeId()).thenReturn(testEmployeeId)
        whenever(teacherRepository.findByUserId(testUserId)).thenReturn(Optional.empty())
        whenever(teacherRepository.save(any())).thenAnswer { it.arguments[0] as Teacher }

        // When
        val result = teacherService.createTeacher(
            userId = testUserId,
            qualifications = listOf("Bachelor's Degree"),
            subjectSpecializations = listOf("Mathematics", "Physics"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = "Science"
        )

        // Then
        assertNotNull(result)
        assertEquals(testEmployeeId, result.employeeId)
        assertEquals(2, result.subjectSpecializations.size)
        assertEquals("Science", result.department)
        verify(employeeIdGenerator).generateEmployeeId()
        verify(teacherRepository).save(any())
    }

    @Test
    fun `should throw exception when teacher already exists`() {
        // Given
        val existingTeacher = Teacher(
            employeeId = "EMP-EXISTING",
            userId = testUserId,
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1)
        )
        whenever(teacherRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingTeacher))

        // When & Then
        assertThrows<IllegalArgumentException> {
            teacherService.createTeacher(
                userId = testUserId,
                qualifications = emptyList(),
                subjectSpecializations = listOf("Science"),
                hireDate = LocalDate.of(2020, 1, 1)
            )
        }

        verify(teacherRepository, never()).save(any())
    }

    @Test
    fun `should throw exception when no subject specializations provided`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            teacherService.createTeacher(
                userId = testUserId,
                qualifications = emptyList(),
                subjectSpecializations = emptyList(),
                hireDate = LocalDate.of(2020, 1, 1)
            )
        }

        verify(teacherRepository, never()).save(any())
    }

    @Test
    fun `should find teacher by id`() {
        // Given
        val teacher = Teacher(
            employeeId = testEmployeeId,
            userId = testUserId,
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1)
        )
        whenever(teacherRepository.findById(teacher.id)).thenReturn(Optional.of(teacher))

        // When
        val result = teacherService.getTeacherById(teacher.id)

        // Then
        assertNotNull(result)
        assertEquals(testEmployeeId, result?.employeeId)
        verify(teacherRepository).findById(teacher.id)
    }

    @Test
    fun `should update employment status`() {
        // Given
        val teacher = Teacher(
            employeeId = testEmployeeId,
            userId = testUserId,
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )
        whenever(teacherRepository.findById(teacher.id)).thenReturn(Optional.of(teacher))
        whenever(teacherRepository.save(any())).thenAnswer { it.arguments[0] as Teacher }

        // When
        val result = teacherService.updateEmploymentStatus(teacher.id, EmploymentStatus.ON_LEAVE)

        // Then
        assertEquals(EmploymentStatus.ON_LEAVE, result.employmentStatus)
        verify(teacherRepository).save(teacher)
    }

    @Test
    fun `should get teachers by status`() {
        // Given
        val activeTeacher = Teacher(
            employeeId = "EMP-001",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Math"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )
        whenever(teacherRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE))
            .thenReturn(listOf(activeTeacher))

        // When
        val results = teacherService.getTeachersByStatus(EmploymentStatus.ACTIVE)

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.employmentStatus == EmploymentStatus.ACTIVE })
    }
}

