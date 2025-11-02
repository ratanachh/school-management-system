package com.visor.school.user.service

import com.visor.school.common.exception.ConflictException
import com.visor.school.user.domain.model.Student
import com.visor.school.user.domain.model.StudentStatus
import com.visor.school.user.repository.StudentRepository
import com.visor.school.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findById(id: UUID): Student {
        logger.debug("Finding student by id: {}", id)
        return studentRepository.findByIdOrThrow(id)
    }

    @Transactional(readOnly = true)
    fun findByStudentNumber(studentNumber: String): Student? {
        logger.debug("Finding student by number: {}", studentNumber)
        return studentRepository.findByStudentNumber(studentNumber).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findAll(page: Int, size: Int): Page<Student> {
        logger.debug("Finding all students, page: {}, size: {}", page, size)
        val pageRequest = PageRequest.of(page, size.coerceAtMost(20))
        return studentRepository.findAll(pageRequest)
    }

    @Transactional(readOnly = true)
    fun search(searchTerm: String): List<Student> {
        logger.debug("Searching students with term: {}", searchTerm)
        return studentRepository.search(searchTerm)
    }

    @Transactional
    fun create(student: Student): Student {
        logger.info("Creating student with number: {}", student.studentNumber)
        
        // Check if student number already exists
        if (studentRepository.existsByStudentNumber(student.studentNumber)) {
            throw ConflictException("Student with number ${student.studentNumber} already exists", "Student")
        }

        val savedStudent = studentRepository.save(student)
        logger.info("Student created successfully with id: {}", savedStudent.id)
        return savedStudent
    }

    @Transactional
    fun update(id: UUID, updatedStudent: Student): Student {
        logger.info("Updating student with id: {}", id)
        val existingStudent = studentRepository.findByIdOrThrow(id)
        
        // Update fields
        updatedStudent.studentNumber?.let { existingStudent.studentNumber = it }
        updatedStudent.dateOfBirth?.let { existingStudent.dateOfBirth = it }
        updatedStudent.gender?.let { existingStudent.gender = it }
        updatedStudent.address?.let { existingStudent.address = it }
        updatedStudent.phone?.let { existingStudent.phone = it }
        updatedStudent.parentGuardianName?.let { existingStudent.parentGuardianName = it }
        updatedStudent.parentGuardianEmail?.let { existingStudent.parentGuardianEmail = it }
        updatedStudent.parentGuardianPhone?.let { existingStudent.parentGuardianPhone = it }
        updatedStudent.status?.let { existingStudent.status = it }
        updatedStudent.photoUrl?.let { existingStudent.photoUrl = it }
        
        val savedStudent = studentRepository.save(existingStudent)
        logger.info("Student updated successfully with id: {}", id)
        return savedStudent
    }

    @Transactional
    fun delete(id: UUID) {
        logger.info("Deleting student with id: {}", id)
        val student = studentRepository.findByIdOrThrow(id)
        student.status = StudentStatus.DROPPED
        studentRepository.save(student)
        logger.info("Student deleted successfully with id: {}", id)
    }
}

