package com.visor.school.common.constant

/**
 * Common constants used across services
 */
object Constants {
    
    // Pagination defaults
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    const val DEFAULT_PAGE_NUMBER = 0
    
    // Date formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_EMAIL_LENGTH = 255
    const val MAX_NAME_LENGTH = 100
    
    // Token expiration (in minutes)
    const val TOKEN_EXPIRATION_MINUTES = 30
    const val REFRESH_TOKEN_EXPIRATION_DAYS = 7
    
    // HTTP Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CORRELATION_ID = "X-Correlation-Id"
    const val HEADER_REQUEST_ID = "X-Request-Id"
    
    // Message Queue
    const val EXCHANGE_SCHOOL_MANAGEMENT = "school.management.exchange"
    const val QUEUE_USER_EVENTS = "school.management.user.events"
}

