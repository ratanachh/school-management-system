package com.visor.school.common.api;

/**
 * Common constants used across services.
 * Avoids magic strings and improves maintainability.
 */
public final class Constants {

    private Constants() {
    }

    // HTTP Headers
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";

    // API Paths
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String HEALTH_PATH = "/health";
    public static final String ACTUATOR_PATH = "/actuator";

    // Error Messages
    public static final String ERROR_RESOURCE_NOT_FOUND = "The requested resource was not found.";
    public static final String ERROR_CONCURRENT_MODIFICATION =
            "The resource was modified by another user. Please refresh and try again.";
    public static final String ERROR_ACCESS_DENIED = "You do not have permission to perform this action.";
    public static final String ERROR_INVALID_STATE = "The operation cannot be performed in the current state.";
    public static final String ERROR_VALIDATION_FAILED = "Input validation failed";

    // Rate Limiting (requests per minute)
    public static final int RATE_LIMIT_DEFAULT = 100;
    public static final int RATE_LIMIT_STRICT = 20;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}
