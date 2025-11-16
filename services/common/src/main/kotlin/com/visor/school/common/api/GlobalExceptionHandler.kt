package com.visor.school.common.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import jakarta.validation.ConstraintViolationException
import java.util.NoSuchElementException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            error = "BAD_REQUEST",
            message = ex.message ?: "Invalid request",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Constraint violation: ${ex.message}", ex)
        val violations = ex.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }
        val errorResponse = ErrorResponse(
            error = "VALIDATION_ERROR",
            message = "Validation failed",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.BAD_REQUEST.value(),
            details = violations
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        val errorResponse = ErrorResponse(
            error = "NOT_FOUND",
            message = ex.message ?: "The requested resource was not found.",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal state: ${ex.message}")
        val errorResponse = ErrorResponse(
            error = "INVALID_STATE",
            message = ex.message ?: "The operation cannot be performed in the current state.",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLockingFailure(
        ex: org.springframework.orm.ObjectOptimisticLockingFailureException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Optimistic locking failure: ${ex.message}")
        val errorResponse = ErrorResponse(
            error = "CONCURRENT_MODIFICATION",
            message = "The resource was modified by another user. Please refresh and try again.",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: org.springframework.security.access.AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: ${ex.message}")
        val errorResponse = ErrorResponse(
            error = "ACCESS_DENIED",
            message = "You do not have permission to perform this action.",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.FORBIDDEN.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            error = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

