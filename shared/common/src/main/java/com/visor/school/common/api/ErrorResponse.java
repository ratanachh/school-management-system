package com.visor.school.common.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;

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
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            HttpStatus status,
            String path
    ) {
        return new ErrorResponse(
            errorCode.name(),
            message,
            Instant.now(),
            path,
            status.value(),
            null
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            HttpStatus status,
            String path,
            Map<String, Object> details
    ) {
        return new ErrorResponse(
            errorCode.name(),
            message,
            Instant.now(),
            path,
            status.value(),
            details
        );
    }

    public ErrorResponse(String error, String message) {
        this(error, message, Instant.now(), null, null, null);
    }

    public ErrorResponse(String error, String message, Instant timestamp, String path, Integer status) {
        this(error, message, timestamp, path, status, null);
    }
}
