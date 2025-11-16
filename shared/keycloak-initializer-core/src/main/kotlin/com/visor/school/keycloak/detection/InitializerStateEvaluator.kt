package com.visor.school.keycloak.detection

import com.visor.school.keycloak.model.InitializationDecision
import com.visor.school.keycloak.model.KeycloakBlueprint

/**
 * Determines whether the Keycloak initializer should apply the provided blueprint.
 */
fun interface InitializerStateEvaluator {
    fun evaluate(blueprint: KeycloakBlueprint): InitializationDecision
}
