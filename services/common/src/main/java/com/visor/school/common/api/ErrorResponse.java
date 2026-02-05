package com.visor.school.common.api;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response DTO
 */
public record ErrorResponse(
        String error,
        String message,
        Instant timestamp,
        String path,
        Integer status,
        Map<String, Object> details
) {
    public ErrorResponse(String error, String message) {
        this(error, message, Instant.now(), null, null, null);
    }

    public ErrorResponse(String error, String message, Instant timestamp, String path, Integer status) {
        this(error, message, timestamp, path, status, null);
    }
}
