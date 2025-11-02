package com.visor.school.common.util

import java.util.regex.Pattern

object ValidationUtil {
    
    private const val EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    
    private const val UUID_PATTERN = 
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

    /**
     * Validate email format
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val pattern = Pattern.compile(EMAIL_PATTERN)
        return pattern.matcher(email).matches()
    }

    /**
     * Validate UUID format
     */
    fun isValidUuid(uuid: String?): Boolean {
        if (uuid.isNullOrBlank()) return false
        val pattern = Pattern.compile(UUID_PATTERN, Pattern.CASE_INSENSITIVE)
        return pattern.matcher(uuid).matches()
    }

    /**
     * Validate string is not blank
     */
    fun isNotBlank(value: String?): Boolean {
        return !value.isNullOrBlank()
    }

    /**
     * Validate minimum length
     */
    fun hasMinLength(value: String?, minLength: Int): Boolean {
        return value?.length ?: 0 >= minLength
    }

    /**
     * Validate maximum length
     */
    fun hasMaxLength(value: String?, maxLength: Int): Boolean {
        return (value?.length ?: 0) <= maxLength
    }
}

