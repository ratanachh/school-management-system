package com.visor.school.common.api;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static String requestPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            HttpStatus status,
            WebRequest request
    ) {
        return ResponseEntity.status(status).body(
            ErrorResponse.of(errorCode, message, status, requestPath(request))
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            HttpStatus status,
            WebRequest request,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(status).body(
            ErrorResponse.of(errorCode, message, status, requestPath(request), details)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        logger.warn("Illegal argument: {}", ex.getMessage(), ex);
        return buildResponse(
            ErrorCode.BAD_REQUEST,
            ex.getMessage() != null ? ex.getMessage() : "Invalid request",
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request
    ) {
        logger.warn("Constraint violation: {}", ex.getMessage(), ex);
        Map<String, Object> details = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> (Object) v.getMessage()));
        return buildResponse(
            ErrorCode.VALIDATION_ERROR,
            Constants.ERROR_VALIDATION_FAILED,
            HttpStatus.BAD_REQUEST,
            request,
            details
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(
            NoSuchElementException ex,
            WebRequest request
    ) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(
            ErrorCode.NOT_FOUND,
            ex.getMessage() != null ? ex.getMessage() : Constants.ERROR_RESOURCE_NOT_FOUND,
            HttpStatus.NOT_FOUND,
            request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request
    ) {
        logger.warn("Illegal state: {}", ex.getMessage());
        return buildResponse(
            ErrorCode.INVALID_STATE,
            ex.getMessage() != null ? ex.getMessage() : Constants.ERROR_INVALID_STATE,
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex,
            WebRequest request
    ) {
        logger.warn("Optimistic locking failure: {}", ex.getMessage());
        return buildResponse(
            ErrorCode.CONCURRENT_MODIFICATION,
            Constants.ERROR_CONCURRENT_MODIFICATION,
            HttpStatus.CONFLICT,
            request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        logger.warn("Access denied: {}", ex.getMessage());
        return buildResponse(
            ErrorCode.ACCESS_DENIED,
            Constants.ERROR_ACCESS_DENIED,
            HttpStatus.FORBIDDEN,
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(
            ErrorCode.INTERNAL_ERROR,
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request
        );
    }
}
