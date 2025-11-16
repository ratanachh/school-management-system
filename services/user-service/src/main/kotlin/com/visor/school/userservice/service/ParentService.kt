package com.visor.school.userservice.service

import com.visor.school.userservice.model.Parent
import com.visor.school.userservice.model.Relationship
import com.visor.school.userservice.repository.ParentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Parent service for managing parent-student relationships
 */
@Service
@Transactional
class ParentService(
    private val parentRepository: ParentRepository
) {
    private val logger = LoggerFactory.getLogger(ParentService::class.java)

    /**
     * Link a student to a parent
     */
    fun linkStudentToParent(
        userId: UUID,
        studentId: UUID,
        relationship: Relationship,
        isPrimary: Boolean = false
    ): Parent {
        logger.info("Linking student $studentId to parent $userId with relationship: $relationship")

        if (parentRepository.existsByUserIdAndStudentId(userId, studentId)) {
            throw IllegalArgumentException("Parent-student relationship already exists")
        }

        val parent = Parent(
            userId = userId,
            studentId = studentId,
            relationship = relationship,
            isPrimary = isPrimary
        )

        val saved = parentRepository.save(parent)
        logger.info("Parent-student relationship created: ${saved.id}")

        return saved
    }

    /**
     * Get all children (students) for a parent
     */
    @Transactional(readOnly = true)
    fun getChildrenByParent(userId: UUID): List<Parent> {
        return parentRepository.findByUserId(userId)
    }

    /**
     * Get all parents for a student
     */
    @Transactional(readOnly = true)
    fun getParentsByStudent(studentId: UUID): List<Parent> {
        return parentRepository.findByStudentId(studentId)
    }

    /**
     * Remove parent-student relationship
     */
    fun unlinkStudentFromParent(userId: UUID, studentId: UUID) {
        logger.info("Unlinking student $studentId from parent $userId")

        val parent = parentRepository.findByUserId(userId)
            .find { it.studentId == studentId }
            ?: throw IllegalArgumentException("Parent-student relationship not found")

        parentRepository.delete(parent)
        logger.info("Parent-student relationship removed")
    }
}

