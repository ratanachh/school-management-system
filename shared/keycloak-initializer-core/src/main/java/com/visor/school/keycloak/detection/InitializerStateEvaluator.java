package com.visor.school.keycloak.detection;

import com.visor.school.keycloak.model.InitializationDecision;
import com.visor.school.keycloak.model.KeycloakBlueprint;

/**
 * Determines whether the Keycloak initializer should apply the provided blueprint.
 */
@FunctionalInterface
public interface InitializerStateEvaluator {
    InitializationDecision evaluate(KeycloakBlueprint blueprint);
}
