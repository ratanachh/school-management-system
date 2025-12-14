package com.visor.school.academicservice.service

import com.visor.school.academicservice.event.StudentEventPublisher
import com.visor.school.academicservice.model.Address
import com.visor.school.academicservice.model.EmergencyContact
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.repository.StudentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Student service with grade level validation (1-12 for K12 system)
 */
@Service
@Transactional
class StudentService(
    private val studentRepository: StudentRepository,
    private val studentIdGenerator: StudentIdGenerator,
    private val studentEventPublisher: StudentEventPublisher
) {
    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    /**
     * Enroll a new student
     * Validates grade level is between 1 and 12
     */
    fun enrollStudent(
        userId: UUID,
        firstName: String,
        lastName: String,
        dateOfBirth: LocalDate,
        gradeLevel: Int,
        address: Address? = null,
        emergencyContact: EmergencyContact? = null
    ): Student {
        logger.info("Enrolling student: $firstName $lastName, grade level: $gradeLevel")

        // Validate grade level
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12 (K12 system), got: $gradeLevel"
        }

        // Generate student ID
        val studentId = studentIdGenerator.generateStudentId()

        // Check if student with this user ID already exists
        if (studentRepository.findByUserId(userId).isPresent) {
            throw IllegalArgumentException("Student with user ID $userId already exists")
        }

        val student = Student(
            studentId = studentId,
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            gradeLevel = gradeLevel,
            enrollmentStatus = EnrollmentStatus.ENROLLED,
            address = address,
            emergencyContact = emergencyContact
        )

        val saved = studentRepository.save(student)
        logger.info("Student enrolled successfully: ${saved.studentId}")

        // Publish student enrolled event
        studentEventPublisher.publishStudentEnrolled(saved)

        return saved
    }

    /**
     * Get student by ID
     */
    @Transactional(readOnly = true)
    fun getStudentById(id: UUID): Student? {
        return studentRepository.findById(id).orElse(null)
    }

    /**
     * Get student by student ID
     */
    @Transactional(readOnly = true)
    fun getStudentByStudentId(studentId: String): Student? {
        return studentRepository.findByStudentId(studentId).orElse(null)
    }

    /**
     * Get student by user ID
     */
    @Transactional(readOnly = true)
    fun getStudentByUserId(userId: UUID): Student? {
        return studentRepository.findByUserId(userId).orElse(null)
    }

    /**
     * Search students by name
     */
    @Transactional(readOnly = true)
    fun searchStudentsByName(name: String): List<Student> {
        return studentRepository.searchByName(name)
    }

    /**
     * Get students by grade level
     */
    @Transactional(readOnly = true)
    fun getStudentsByGradeLevel(gradeLevel: Int): List<Student> {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12, got: $gradeLevel"
        }
        return studentRepository.findByGradeLevel(gradeLevel)
    }

    /**
     * Update student information
     */
    fun updateStudent(
        id: UUID,
        firstName: String? = null,
        lastName: String? = null,
        gradeLevel: Int? = null,
        address: Address? = null,
        emergencyContact: EmergencyContact? = null
    ): Student {
        val student = studentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Student not found: $id") }

        if (firstName != null) {
            // Note: In a full implementation, we'd update the entity fields
            // For now, we'll just update the timestamp
            logger.info("Updating student $id: firstName")
        }

        if (lastName != null) {
            logger.info("Updating student $id: lastName")
        }

        if (gradeLevel != null) {
            require(gradeLevel in 1..12) {
                "Grade level must be between 1 and 12, got: $gradeLevel"
            }
            student.gradeLevel = gradeLevel
            logger.info("Updated student $id: gradeLevel to $gradeLevel")
        }

        student.updatedAt = java.time.Instant.now()
        return studentRepository.save(student)
    }

    /**
     * Update enrollment status
     */
    fun updateEnrollmentStatus(id: UUID, status: EnrollmentStatus): Student {
        val student = studentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Student not found: $id") }

        student.updateEnrollmentStatus(status)
        logger.info("Updated enrollment status for student $id to $status")

        return studentRepository.save(student)
    }

    /**
     * Promote student to next grade
     */
    fun promoteStudent(id: UUID): Student {
        val student = studentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Student not found: $id") }

        student.promoteToNextGrade()
        logger.info("Promoted student $id to grade ${student.gradeLevel}")

        return studentRepository.save(student)
    }
}
