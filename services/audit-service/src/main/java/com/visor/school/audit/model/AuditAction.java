package com.visor.school.audit.model;

/**
 * Audit action enumeration for security events
 */
public enum AuditAction {
    AUTHENTICATION,
    ACCESS_ATTEMPT,
    DATA_MODIFICATION,
    DATA_CREATION,
    DATA_DELETION,
    PASSWORD_RESET,
    EMAIL_VERIFICATION
}
