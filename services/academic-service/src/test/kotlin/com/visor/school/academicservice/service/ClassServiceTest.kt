package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.repository.ClassRepository
import com.visor.school.academicservice.repository.TeacherAssignmentRepository
import com.visor.school.academicservice.repository.TeacherRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ClassServiceTest {

    @Mock
    private lateinit var classRepository: ClassRepository

    @Mock
    private lateinit var teacherRepository: TeacherRepository

    @Mock
    private lateinit var teacherAssignmentRepository: TeacherAssignmentRepository

    @InjectMocks
    private lateinit var classService: ClassService

    private val testTeacherId = UUID.randomUUID()
    private val testClassId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        classService = ClassService(classRepository, teacherRepository, teacherAssignmentRepository)
    }

    @Test
    fun `should create homeroom class for grades 1-6`() {
        // Given
        val teacher = Teacher(
            id = testTeacherId,
            employeeId = "EMP-001",
            userId = testTeacherId,
            subjectSpecializations = listOf("General"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )
        whenever(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher))
        whenever(classRepository.findByAcademicYearAndTermAndTypeAndGrade(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(classRepository.save(any())).thenAnswer { it.arguments[0] as Class }

        // When
        val result = classService.createHomeroomClass(
            className = "Grade 3 Homeroom",
            gradeLevel = 3,
            homeroomTeacherId = testTeacherId,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // Then
        assertNotNull(result)
        assertEquals(ClassType.HOMEROOM, result.classType)
        assertEquals(3, result.gradeLevel)
        assertEquals(testTeacherId, result.homeroomTeacherId)
        assertNull(result.subject)
        verify(classRepository).save(any())
    }

    @Test
    fun `should throw exception for homeroom class with grade above 6`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            classService.createHomeroomClass(
                className = "Invalid Homeroom",
                gradeLevel = 7,
                homeroomTeacherId = testTeacherId,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }

        verify(classRepository, never()).save(any())
    }

    @Test
    fun `should throw exception when homeroom class already exists for grade`() {
        // Given
        val existingClass = Class(
            className = "Existing Homeroom",
            classType = ClassType.HOMEROOM,
            subject = null,
            gradeLevel = 3,
            homeroomTeacherId = UUID.randomUUID(),
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            status = ClassStatus.SCHEDULED,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = null
        )
        whenever(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(mock()))
        whenever(classRepository.findByAcademicYearAndTermAndTypeAndGrade(any(), any(), any(), any()))
            .thenReturn(listOf(existingClass))

        // When & Then
        assertThrows<IllegalArgumentException> {
            classService.createHomeroomClass(
                className = "Duplicate Homeroom",
                gradeLevel = 3,
                homeroomTeacherId = testTeacherId,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should create subject class for all grades`() {
        // Given
        whenever(classRepository.save(any())).thenAnswer { it.arguments[0] as Class }

        // When
        val result = classService.createSubjectClass(
            className = "Mathematics 101",
            subject = "Mathematics",
            gradeLevel = 10,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        // Then
        assertNotNull(result)
        assertEquals(ClassType.SUBJECT, result.classType)
        assertEquals("Mathematics", result.subject)
        assertEquals(10, result.gradeLevel)
        assertNull(result.homeroomTeacherId)
        verify(classRepository).save(any())
    }

    @Test
    fun `should assign class teacher for grades 7-12`() {
        // Given
        val classEntity = Class(
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            status = ClassStatus.SCHEDULED,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = null
        )
        val teacher = Teacher(
            id = testTeacherId,
            employeeId = "EMP-001",
            userId = testTeacherId,
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )
        val assignment = TeacherAssignment(
            teacherId = testTeacherId,
            classId = classEntity.id,
            isClassTeacher = false
        )

        whenever(classRepository.findById(classEntity.id)).thenReturn(Optional.of(classEntity))
        whenever(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher))
        whenever(teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, classEntity.id))
            .thenReturn(listOf(assignment))
        whenever(teacherAssignmentRepository.findClassTeacherByClassId(classEntity.id))
            .thenReturn(emptyList())

        // When
        val result = classService.assignClassTeacher(classEntity.id, testTeacherId)

        // Then
        assertNotNull(result)
        verify(classRepository).findById(classEntity.id)
    }

    @Test
    fun `should throw exception when assigning class teacher to grades 1-6`() {
        // Given
        val classEntity = Class(
            className = "Grade 3 Homeroom",
            classType = ClassType.HOMEROOM,
            subject = null,
            gradeLevel = 3,
            homeroomTeacherId = UUID.randomUUID(),
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            status = ClassStatus.SCHEDULED,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = null
        )
        whenever(classRepository.findById(classEntity.id)).thenReturn(Optional.of(classEntity))

        // When & Then
        assertThrows<IllegalArgumentException> {
            classService.assignClassTeacher(classEntity.id, testTeacherId)
        }
    }

    @Test
    fun `should throw exception when teacher not assigned to class`() {
        // Given
        val classEntity = Class(
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            status = ClassStatus.SCHEDULED,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = null
        )
        val teacher = Teacher(
            id = testTeacherId,
            employeeId = "EMP-001",
            userId = testTeacherId,
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )

        whenever(classRepository.findById(classEntity.id)).thenReturn(Optional.of(classEntity))
        whenever(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher))
        whenever(teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, classEntity.id))
            .thenReturn(emptyList())

        // When & Then
        assertThrows<IllegalArgumentException> {
            classService.assignClassTeacher(classEntity.id, testTeacherId)
        }
    }

    @Test
    fun `should update class status`() {
        // Given
        val classEntity = Class(
            className = "Test Class",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 5,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            status = ClassStatus.SCHEDULED,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = null
        )
        whenever(classRepository.findById(classEntity.id)).thenReturn(Optional.of(classEntity))
        whenever(classRepository.save(any())).thenAnswer { it.arguments[0] as Class }

        // When
        val result = classService.updateClassStatus(classEntity.id, ClassStatus.IN_PROGRESS)

        // Then
        assertEquals(ClassStatus.IN_PROGRESS, result.status)
        verify(classRepository).save(classEntity)
    }
}
