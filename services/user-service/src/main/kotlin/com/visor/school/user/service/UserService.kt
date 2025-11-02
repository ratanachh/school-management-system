package com.visor.school.user.service

import com.visor.school.common.constant.Constants
import com.visor.school.common.exception.ConflictException
import com.visor.school.common.exception.ResourceNotFoundException
import com.visor.school.common.util.ValidationUtil
import com.visor.school.events.user.UserCreatedEvent
import com.visor.school.events.user.UserUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.visor.school.user.domain.model.User
import com.visor.school.user.domain.model.UserStatus
import com.visor.school.user.repository.UserRepository
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val rabbitTemplate: RabbitTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findById(id: UUID): User {
        logger.debug("Finding user by id: {}", id)
        return userRepository.findByIdOrThrow(id)
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? {
        logger.debug("Finding user by email: {}", email)
        ValidationUtil.isValidEmail(email)
        return userRepository.findByEmail(email).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findAll(page: Int, size: Int): Page<User> {
        logger.debug("Finding all users, page: {}, size: {}", page, size)
        val pageRequest = PageRequest.of(page, size.coerceAtMost(Constants.MAX_PAGE_SIZE))
        return userRepository.findAll(pageRequest)
    }

    @Transactional(readOnly = true)
    fun search(searchTerm: String): List<User> {
        logger.debug("Searching users with term: {}", searchTerm)
        return userRepository.search(searchTerm)
    }

    @Transactional
    fun create(user: User): User {
        logger.info("Creating user with email: {}", user.email)
        
        // Validate email format
        if (!ValidationUtil.isValidEmail(user.email)) {
            throw com.visor.school.common.exception.ValidationException("Invalid email format")
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.email)) {
            throw ConflictException("User with email ${user.email} already exists", "User")
        }

        val savedUser = userRepository.save(user)
        
        // Publish event
        publishUserCreatedEvent(savedUser)
        
        logger.info("User created successfully with id: {}", savedUser.id)
        return savedUser
    }

    @Transactional
    fun update(id: UUID, updatedUser: User): User {
        logger.info("Updating user with id: {}", id)
        
        val existingUser = userRepository.findByIdOrThrow(id)
        
        // Update only provided fields
        updatedUser.email?.let { existingUser.email = it }
        updatedUser.username?.let { existingUser.username = it }
        updatedUser.firstName?.let { existingUser.firstName = it }
        updatedUser.lastName?.let { existingUser.lastName = it }
        updatedUser.status?.let { existingUser.status = it }
        updatedUser.emailVerified?.let { existingUser.emailVerified = it }
        updatedUser.roles?.let { existingUser.roles = it }
        
        val savedUser = userRepository.save(existingUser)
        
        // Publish event
        publishUserUpdatedEvent(savedUser)
        
        logger.info("User updated successfully with id: {}", id)
        return savedUser
    }

    @Transactional
    fun delete(id: UUID) {
        logger.info("Deleting user with id: {}", id)
        val user = userRepository.findByIdOrThrow(id)
        user.status = UserStatus.DELETED
        userRepository.save(user)
        logger.info("User deleted successfully with id: {}", id)
    }

    @Transactional
    fun verifyEmail(id: UUID) {
        logger.info("Verifying email for user with id: {}", id)
        val user = userRepository.findByIdOrThrow(id)
        user.emailVerified = true
        userRepository.save(user)
        logger.info("Email verified for user with id: {}", id)
    }

    private fun publishUserCreatedEvent(user: User) {
        try {
            val event = UserCreatedEvent(
                aggregateId = user.id.toString(),
                payload = com.visor.school.events.user.UserCreatedPayload(
                    userId = user.id.toString(),
                    email = user.email,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    role = user.roles?.split(",")?.firstOrNull() ?: "USER"
                )
            )
            rabbitTemplate.convertAndSend(Constants.EXCHANGE_SCHOOL_MANAGEMENT, Constants.QUEUE_USER_EVENTS, event)
            logger.debug("User created event published for user: {}", user.id)
        } catch (e: Exception) {
            logger.error("Failed to publish user created event for user: {}", user.id, e)
            // Don't fail the transaction if event publishing fails
        }
    }

    private fun publishUserUpdatedEvent(user: User) {
        try {
            val event = UserUpdatedEvent(
                aggregateId = user.id.toString(),
                payload = com.visor.school.events.user.UserUpdatedPayload(
                    userId = user.id.toString(),
                    email = user.email,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    status = user.status.name
                )
            )
            rabbitTemplate.convertAndSend(Constants.EXCHANGE_SCHOOL_MANAGEMENT, Constants.QUEUE_USER_EVENTS, event)
            logger.debug("User updated event published for user: {}", user.id)
        } catch (e: Exception) {
            logger.error("Failed to publish user updated event for user: {}", user.id, e)
            // Don't fail the transaction if event publishing fails
        }
    }
}

