package com.visor.school.keycloak.model;

/**
 * Result of evaluating and potentially executing the initializer.
 */
public record InitializationOutcome(
    InitializationStatus status,
    String message
) {
    public static InitializationOutcome skipped(String reason) {
        return new InitializationOutcome(InitializationStatus.SKIPPED, reason);
    }

    public static InitializationOutcome applied(String message) {
        return new InitializationOutcome(InitializationStatus.APPLIED, message);
    }

    public static InitializationOutcome applied() {
        return applied("Keycloak configuration applied");
    }
}
