package com.visor.school.common.api

import java.time.Instant

/**
 * Standard API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        @JvmOverloads
        @JvmStatic
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(true, data, message)
        }

        @JvmStatic
        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(false, null, message)
        }
    }
}

