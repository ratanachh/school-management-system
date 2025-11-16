package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.Address
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.repository.StudentRepository
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
class StudentServiceTest {

    @Mock
    private lateinit var studentRepository: StudentRepository

    @Mock
    private lateinit var studentIdGenerator: StudentIdGenerator

    @Mock
    private lateinit var studentEventPublisher: StudentEventPublisher

    @InjectMocks
    private lateinit var studentService: StudentService

    private val testUserId = UUID.randomUUID()
    private val testStudentId = "STU-2025-001"

    @BeforeEach
    fun setup() {
        studentService = StudentService(studentRepository, studentIdGenerator, studentEventPublisher)
    }

    @Test
    fun `should enroll student successfully with valid grade level`() {
        // Given
        whenever(studentIdGenerator.generateStudentId()).thenReturn(testStudentId)
        whenever(studentRepository.findByUserId(testUserId)).thenReturn(Optional.empty())
        whenever(studentRepository.save(any())).thenAnswer { it.arguments[0] as Student }
        doNothing().whenever(studentEventPublisher).publishStudentEnrolled(any())

        // When
        val result = studentService.enrollStudent(
            userId = testUserId,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result.studentId)
        assertEquals(5, result.gradeLevel)
        assertEquals(EnrollmentStatus.ENROLLED, result.enrollmentStatus)
        verify(studentIdGenerator).generateStudentId()
        verify(studentRepository).save(any())
        verify(studentEventPublisher).publishStudentEnrolled(any())
    }

    @Test
    fun `should throw exception for grade level below 1`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            studentService.enrollStudent(
                userId = testUserId,
                firstName = "Invalid",
                lastName = "Grade",
                dateOfBirth = LocalDate.of(2020, 1, 1),
                gradeLevel = 0
            )
        }

        verify(studentRepository, never()).save(any())
    }

    @Test
    fun `should throw exception for grade level above 12`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            studentService.enrollStudent(
                userId = testUserId,
                firstName = "Invalid",
                lastName = "Grade",
                dateOfBirth = LocalDate.of(2000, 1, 1),
                gradeLevel = 13
            )
        }

        verify(studentRepository, never()).save(any())
    }

    @Test
    fun `should throw exception when student already exists`() {
        // Given
        val existingStudent = Student(
            studentId = "STU-EXISTING",
            userId = testUserId,
            firstName = "Existing",
            lastName = "Student",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        whenever(studentRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingStudent))

        // When & Then
        assertThrows<IllegalArgumentException> {
            studentService.enrollStudent(
                userId = testUserId,
                firstName = "New",
                lastName = "Student",
                dateOfBirth = LocalDate.of(2010, 1, 1),
                gradeLevel = 5
            )
        }

        verify(studentRepository, never()).save(any())
    }

    @Test
    fun `should find student by id`() {
        // Given
        val student = Student(
            studentId = testStudentId,
            userId = testUserId,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        whenever(studentRepository.findById(student.id)).thenReturn(Optional.of(student))

        // When
        val result = studentService.findById(student.id)

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result?.studentId)
        verify(studentRepository).findById(student.id)
    }

    @Test
    fun `should search students by name`() {
        // Given
        val student1 = Student(
            studentId = "STU-001",
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        val student2 = Student(
            studentId = "STU-002",
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        whenever(studentRepository.findAll()).thenReturn(listOf(student1, student2))

        // When
        val results = studentService.searchStudents("John")

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.firstName.contains("John", ignoreCase = true) })
    }

    @Test
    fun `should update student grade level`() {
        // Given
        val student = Student(
            studentId = testStudentId,
            userId = testUserId,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        whenever(studentRepository.findById(student.id)).thenReturn(Optional.of(student))
        whenever(studentRepository.save(any())).thenAnswer { it.arguments[0] as Student }

        // When
        studentService.updateGradeLevel(student.id, 6)

        // Then
        assertEquals(6, student.gradeLevel)
        verify(studentRepository).save(student)
    }

    @Test
    fun `should enroll student with address and emergency contact`() {
        // Given
        val address = Address(
            street = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62701",
            country = "USA"
        )
        val emergencyContact = EmergencyContact(
            name = "Jane Doe",
            relationship = "Mother",
            phoneNumber = "555-1234",
            email = "jane@example.com"
        )

        whenever(studentIdGenerator.generateStudentId()).thenReturn(testStudentId)
        whenever(studentRepository.findByUserId(testUserId)).thenReturn(Optional.empty())
        whenever(studentRepository.save(any())).thenAnswer { it.arguments[0] as Student }
        doNothing().whenever(studentEventPublisher).publishStudentEnrolled(any())

        // When
        val result = studentService.enrollStudent(
            userId = testUserId,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5,
            address = address,
            emergencyContact = emergencyContact
        )

        // Then
        assertNotNull(result.address)
        assertEquals("Springfield", result.address?.city)
        assertNotNull(result.emergencyContact)
        assertEquals("Jane Doe", result.emergencyContact?.name)
    }
}

