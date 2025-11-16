package com.visor.school.common.api

/**
 * Common constants used across services
 * Avoids magic strings and improves maintainability
 */
object Constants {
    // HTTP Headers
    const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    const val USER_ID_HEADER = "X-User-Id"
    
    // API Paths
    const val API_V1_PREFIX = "/api/v1"
    const val HEALTH_PATH = "/health"
    const val ACTUATOR_PATH = "/actuator"
    
    // Error Messages
    const val ERROR_RESOURCE_NOT_FOUND = "The requested resource was not found."
    const val ERROR_CONCURRENT_MODIFICATION = "The resource was modified by another user. Please refresh and try again."
    const val ERROR_ACCESS_DENIED = "You do not have permission to perform this action."
    const val ERROR_INVALID_STATE = "The operation cannot be performed in the current state."
    const val ERROR_VALIDATION_FAILED = "Input validation failed"
    
    // Rate Limiting
    const val RATE_LIMIT_DEFAULT = 100 // requests per minute
    const val RATE_LIMIT_STRICT = 20 // requests per minute for sensitive operations
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
}

