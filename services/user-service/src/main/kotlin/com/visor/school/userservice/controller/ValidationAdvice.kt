package com.visor.school.userservice.controller

import com.visor.school.common.api.ErrorResponse
import com.visor.school.common.api.GlobalExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Validation advice for user service
 * Extends GlobalExceptionHandler with service-specific error handling
 */
@RestControllerAdvice
class ValidationAdvice : GlobalExceptionHandler() {

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
}

