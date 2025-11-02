package com.visor.school.common.dto

import java.time.Instant

/**
 * Standard API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Instant = Instant.now(),
    val errors: List<ErrorDetail>? = null
) {
    companion object {
        fun <T> success(data: T?, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> error(message: String, errors: List<ErrorDetail>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                errors = errors
            )
        }
    }
}

data class ErrorDetail(
    val field: String? = null,
    val message: String,
    val code: String? = null
)

