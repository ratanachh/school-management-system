package com.visor.school.common.exception

/**
 * Base exception for business logic errors
 */
open class BusinessException(
    message: String,
    val code: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Resource not found exception
 */
class ResourceNotFoundException(
    message: String,
    resource: String? = null
) : BusinessException(
    message = message,
    code = "RESOURCE_NOT_FOUND"
) {
    val resourceType = resource
}

/**
 * Validation exception
 */
class ValidationException(
    message: String,
    val errors: Map<String, String>? = null
) : BusinessException(
    message = message,
    code = "VALIDATION_ERROR"
)

/**
 * Unauthorized access exception
 */
class UnauthorizedException(
    message: String = "Unauthorized access"
) : BusinessException(
    message = message,
    code = "UNAUTHORIZED"
)

/**
 * Conflict exception
 */
class ConflictException(
    message: String,
    val resource: String? = null
) : BusinessException(
    message = message,
    code = "CONFLICT"
)

