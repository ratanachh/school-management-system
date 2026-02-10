package com.visor.school.userservice.controller;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import com.visor.school.common.api.ErrorResponse;
import com.visor.school.common.api.GlobalExceptionHandler;
import com.visor.school.userservice.integration.KeycloakException;
import com.visor.school.userservice.integration.UserAlreadyExistsException;

/**
 * Validation advice for user service
 * Extends GlobalExceptionHandler with service-specific error handling
 */
@RestControllerAdvice
public class ValidationAdvice extends GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        Map<String, Object> errors = ex.getBindingResult().getAllErrors().stream()
            .collect(Collectors.toMap(
                error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField();
                    } else {
                        return error.getObjectName();
                    }
                },
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation error"
            ));

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed for request",
            Instant.now(),
            null, // Path handled by GlobalHandler helper? No, explicit null in Kotlin. Record path is 4th.
            HttpStatus.BAD_REQUEST.value(),
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakException(
        KeycloakException ex,
        WebRequest request
    ) {
        Throwable cause = ex.getCause();
        HttpStatus httpStatus;
        String errorCode;
        String message;

        if (cause instanceof HttpClientErrorException.Unauthorized) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            errorCode = "AUTHENTICATION_FAILED";
            message = "Invalid email or password";
            logger.warn("Authentication failed: {}", ex.getMessage());
        } else if (cause instanceof HttpClientErrorException.BadRequest) {
            httpStatus = HttpStatus.BAD_REQUEST;
            errorCode = "BAD_REQUEST";
            message = extractErrorMessage(ex, "Invalid request");
            logger.warn("Keycloak bad request: {}", ex.getMessage());
        } else if (cause instanceof HttpClientErrorException.NotFound) {
            httpStatus = HttpStatus.NOT_FOUND;
            errorCode = "NOT_FOUND";
            message = extractErrorMessage(ex, "Resource not found");
            logger.warn("Keycloak resource not found: {}", ex.getMessage());
        } else if (cause instanceof HttpClientErrorException.Conflict) {
            httpStatus = HttpStatus.CONFLICT;
            errorCode = "CONFLICT";
            message = extractErrorMessage(ex, "Resource conflict");
            logger.warn("Keycloak conflict: {}", ex.getMessage());
        } else if (cause instanceof HttpClientErrorException) {
            httpStatus = HttpStatus.valueOf(((HttpClientErrorException) cause).getStatusCode().value());
            errorCode = "KEYCLOAK_ERROR";
            message = extractErrorMessage(ex, "Keycloak operation failed");
            logger.error("Keycloak error ({}): {}", httpStatus.value(), ex.getMessage(), ex);
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "KEYCLOAK_ERROR";
            message = extractErrorMessage(ex, "An error occurred while communicating with authentication service");
            logger.error("Keycloak error: {}", ex.getMessage(), ex);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            errorCode,
            message,
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            httpStatus.value(),
            null
        );

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
        UserAlreadyExistsException ex,
        WebRequest request
    ) {
        logger.warn("User already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "USER_ALREADY_EXISTS",
            ex.getMessage() != null ? ex.getMessage() : "User already exists",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            HttpStatus.CONFLICT.value(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    private String extractErrorMessage(KeycloakException ex, String defaultMessage) {
        if (ex.getMessage() != null && ex.getMessage().contains("Authentication failed")) {
            return "Invalid email or password";
        }
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        return defaultMessage;
    }
}
