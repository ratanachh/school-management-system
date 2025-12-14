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

    @InjectMocks
    private lateinit var studentService: StudentService

    private val testStudent = Student(
        userId = UUID.randomUUID().toString(),
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
        whenever(studentRepository.save(any<Student>())).thenReturn(testStudent)

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
        verify(studentEventPublisher).publishStudentEnrolledEvent(enrolledStudent)
    }

    @Test
    fun `update student should save and publish event`() {
        // Given
        val studentId = UUID.randomUUID()
        whenever(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent))
        whenever(studentRepository.save(any<Student>())).thenReturn(testStudent)

        // When
        val updatedStudent = studentService.updateStudent(studentId.toString(), "Jane", null, null)

        // Then
        assertEquals("Jane", updatedStudent.firstName)
        verify(studentRepository).save(any<Student>())
        verify(studentEventPublisher).publishStudentUpdatedEvent(updatedStudent)
    }

    @Test
    fun `search students should return a page of students`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        whenever(studentRepository.findByNameContainingIgnoreCase("John", pageable))
            .thenReturn(PageImpl(listOf(testStudent)))

        // When
        val students = studentService.searchStudents("John", 1)

        // Then
        assertFalse(students.isEmpty())
        assertEquals(1, students.size)
        verify(studentRepository).findByNameContainingIgnoreCase("John", pageable)
    }

    @Test
    fun `get students by grade should return a list`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        whenever(studentRepository.findByGradeLevel(5, pageable))
            .thenReturn(PageImpl(listOf(testStudent)))

        // When
        val students = studentService.getStudentsByGrade(5, 1)

        // Then
        assertFalse(students.isEmpty())
        assertEquals(1, students.size)
        verify(studentRepository).findByGradeLevel(5, pageable)
    }
}
