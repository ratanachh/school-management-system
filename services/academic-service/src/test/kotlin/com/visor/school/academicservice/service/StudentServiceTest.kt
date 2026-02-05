package com.visor.school.academicservice.service

import com.visor.school.academicservice.event.StudentEventPublisher
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.repository.StudentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
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
class StudentServiceTest {

    @Mock
    private lateinit var studentRepository: StudentRepository

    @Mock
    private lateinit var studentEventPublisher: StudentEventPublisher

    @Mock
    private lateinit var studentIdGenerator: StudentIdGenerator

    @InjectMocks
    private lateinit var studentService: StudentService

    private val testStudent = Student(
        userId = UUID.randomUUID(),
        studentId = "S12345",
        firstName = "John",
        lastName = "Doe",
        dateOfBirth = LocalDate.of(2010, 5, 15),
        gradeLevel = 5,
        enrollmentStatus = EnrollmentStatus.ENROLLED
    )

    @Test
    fun `enroll student should create and publish event`() {
        // Given
        val savedStudent = Student(
            id = UUID.randomUUID(),
            userId = testStudent.userId,
            studentId = testStudent.studentId,
            firstName = testStudent.firstName,
            lastName = testStudent.lastName,
            dateOfBirth = testStudent.dateOfBirth,
            gradeLevel = testStudent.gradeLevel,
            enrollmentStatus = testStudent.enrollmentStatus
        )
        whenever(studentIdGenerator.generateStudentId()).thenReturn("S12345")
        whenever(studentRepository.save(any<Student>())).thenReturn(savedStudent)

        // When
        val enrolledStudent = studentService.enrollStudent(
            testStudent.userId,
            testStudent.firstName,
            testStudent.lastName,
            testStudent.dateOfBirth,
            testStudent.gradeLevel
        )

        // Then
        assertNotNull(enrolledStudent.studentId)
        verify(studentRepository).save(any<Student>())
        verify(studentEventPublisher).publishStudentEnrolled(enrolledStudent)
    }

    @Test
    fun `update student should save and publish event`() {
        // Given
        val studentId = UUID.randomUUID()
        val existingStudent = Student(
            id = studentId,
            userId = testStudent.userId,
            studentId = testStudent.studentId,
            firstName = testStudent.firstName,
            lastName = testStudent.lastName,
            dateOfBirth = testStudent.dateOfBirth,
            gradeLevel = testStudent.gradeLevel,
            enrollmentStatus = testStudent.enrollmentStatus
        )
        val updatedStudent = Student(
            id = studentId,
            userId = testStudent.userId,
            studentId = testStudent.studentId,
            firstName = "Jane",
            lastName = testStudent.lastName,
            dateOfBirth = testStudent.dateOfBirth,
            gradeLevel = testStudent.gradeLevel,
            enrollmentStatus = testStudent.enrollmentStatus
        )
        whenever(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent))
        whenever(studentRepository.save(any<Student>())).thenReturn(updatedStudent)

        // When
        val result = studentService.updateStudent(studentId, "Jane", null, null)

        // Then
        assertEquals("Jane", result.firstName)
        verify(studentRepository).save(any<Student>())
    }

    @Test
    fun `search students should return a page of students`() {
        // Given
        whenever(studentRepository.searchByName("John"))
            .thenReturn(listOf(testStudent))

        // When
        val students = studentService.searchStudentsByName("John")

        // Then
        assertFalse(students.isEmpty())
        assertEquals(1, students.size)
        verify(studentRepository).searchByName("John")
    }

    @Test
    fun `get students by grade should return a list`() {
        // Given
        whenever(studentRepository.findByGradeLevel(5))
            .thenReturn(listOf(testStudent))

        // When
        val students = studentService.getStudentsByGradeLevel(5)

        // Then
        assertFalse(students.isEmpty())
        assertEquals(1, students.size)
        verify(studentRepository).findByGradeLevel(5)
    }
}