package com.visor.school.common.api;

/**
 * Exception thrown when optimistic locking conflict occurs
 */
public class OptimisticLockingException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "The resource was modified by another user. Please refresh and try again.";

    public OptimisticLockingException() {
        super(DEFAULT_MESSAGE);
    }

    public OptimisticLockingException(String message) {
        super(message);
    }

    public OptimisticLockingException(String message, Throwable cause) {
        super(message, cause);
    }
}
