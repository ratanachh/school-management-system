package com.visor.school.common.api

import java.time.Instant

/**
 * Standard error response DTO
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val path: String? = null,
    val status: Int? = null,
    val details: Map<String, Any>? = null
)

