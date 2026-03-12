package com.visor.school.common.api;

import java.time.Instant;
import java.util.List;

/**
 * Validation error response for input validation failures.
 * Provides detailed field-level error information.
 */
public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<FieldError> fieldErrors,
        List<String> globalErrors
) {
    public ValidationErrorResponse(Instant timestamp, int status, String path) {
        this(timestamp, status, "Validation Error", "Input validation failed", path, null, List.of(), List.of());
    }

    public record FieldError(String field, Object rejectedValue, String message, String code) {
        public FieldError(String field, Object rejectedValue, String message) {
            this(field, rejectedValue, message, null);
        }
    }
}
