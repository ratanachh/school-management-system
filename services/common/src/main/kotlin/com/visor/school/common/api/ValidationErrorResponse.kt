package com.visor.school.common.api

import java.time.Instant

/**
 * Validation error response for input validation failures
 * Provides detailed field-level error information
 */
data class ValidationErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String = "Validation Error",
    val message: String = "Input validation failed",
    val path: String,
    val correlationId: String? = null,
    val fieldErrors: List<FieldError> = emptyList(),
    val globalErrors: List<String> = emptyList()
) {
    data class FieldError(
        val field: String,
        val rejectedValue: Any?,
        val message: String,
        val code: String? = null
    )
}

