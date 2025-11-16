package com.visor.school.common.api

/**
 * Exception thrown when optimistic locking conflict occurs
 */
class OptimisticLockingException(
    message: String = "The resource was modified by another user. Please refresh and try again.",
    cause: Throwable? = null
) : RuntimeException(message, cause)

