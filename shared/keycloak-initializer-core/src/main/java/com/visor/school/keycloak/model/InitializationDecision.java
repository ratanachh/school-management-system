package com.visor.school.keycloak.model;

/**
 * Decision produced by the state evaluator describing whether provisioning should occur.
 */
public record InitializationDecision(
    boolean performProvisioning,
    String reason
) {
    public InitializationDecision(boolean performProvisioning) {
        this(performProvisioning, null);
    }
}
