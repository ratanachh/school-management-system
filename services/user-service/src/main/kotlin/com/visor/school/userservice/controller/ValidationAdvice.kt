package com.visor.school.userservice.controller

import com.visor.school.common.api.ErrorResponse
import com.visor.school.common.api.GlobalExceptionHandler
import com.visor.school.userservice.integration.KeycloakException
import com.visor.school.userservice.integration.UserAlreadyExistsException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.WebRequest
import java.time.Instant

/**
 * Validation advice for user service
 * Extends GlobalExceptionHandler with service-specific error handling
 */
@RestControllerAdvice
class ValidationAdvice : GlobalExceptionHandler() {

    private val logger = LoggerFactory.getLogger(ValidationAdvice::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors: Map<String, String> = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            fieldName to (error.defaultMessage ?: "Validation error")
        }

        val errorResponse = ErrorResponse(
            error = "VALIDATION_ERROR",
            message = "Validation failed for request",
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            details = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(KeycloakException::class)
    fun handleKeycloakException(
        ex: KeycloakException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val cause = ex.cause
        val httpStatus: HttpStatus
        val errorCode: String
        val message: String

        when {
            cause is HttpClientErrorException.Unauthorized -> {
                httpStatus = HttpStatus.UNAUTHORIZED
                errorCode = "AUTHENTICATION_FAILED"
                message = "Invalid email or password"
                logger.warn("Authentication failed: ${ex.message}")
            }
            cause is HttpClientErrorException.BadRequest -> {
                httpStatus = HttpStatus.BAD_REQUEST
                errorCode = "BAD_REQUEST"
                message = extractErrorMessage(ex, "Invalid request")
                logger.warn("Keycloak bad request: ${ex.message}")
            }
            cause is HttpClientErrorException.NotFound -> {
                httpStatus = HttpStatus.NOT_FOUND
                errorCode = "NOT_FOUND"
                message = extractErrorMessage(ex, "Resource not found")
                logger.warn("Keycloak resource not found: ${ex.message}")
            }
            cause is HttpClientErrorException.Conflict -> {
                httpStatus = HttpStatus.CONFLICT
                errorCode = "CONFLICT"
                message = extractErrorMessage(ex, "Resource conflict")
                logger.warn("Keycloak conflict: ${ex.message}")
            }
            cause is HttpClientErrorException -> {
                val statusCode = cause.statusCode
                httpStatus = HttpStatus.valueOf(statusCode.value())
                errorCode = "KEYCLOAK_ERROR"
                message = extractErrorMessage(ex, "Keycloak operation failed")
                logger.error("Keycloak error (${statusCode.value()}): ${ex.message}", ex)
            }
            else -> {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
                errorCode = "KEYCLOAK_ERROR"
                message = extractErrorMessage(ex, "An error occurred while communicating with authentication service")
                logger.error("Keycloak error: ${ex.message}", ex)
            }
        }

        val errorResponse = ErrorResponse(
            error = errorCode,
            message = message,
            timestamp = Instant.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = httpStatus.value()
        )

        return ResponseEntity(errorResponse, httpStatus)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(
        ex: UserAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("User already exists: ${ex.message}")
        val errorResponse = ErrorResponse(
            error = "USER_ALREADY_EXISTS",
            message = ex.message ?: "User already exists",
            timestamp = Instant.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    private fun extractErrorMessage(ex: KeycloakException, defaultMessage: String): String {
        return when {
            ex.message?.contains("Authentication failed") == true -> "Invalid email or password"
            ex.message?.isNotBlank() == true -> ex.message!!
            else -> defaultMessage
        }
    }
}

