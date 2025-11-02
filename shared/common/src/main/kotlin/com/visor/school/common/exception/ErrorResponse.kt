package com.visor.school.common.exception

import com.visor.school.common.dto.ErrorDetail
import java.time.Instant

/**
 * Error response for exception handling
 */
data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val errors: List<ErrorDetail>? = null
)

