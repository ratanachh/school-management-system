package com.visor.school.keycloak.detection

import com.visor.school.keycloak.model.InitializationDecision
import com.visor.school.keycloak.model.KeycloakBlueprint
import com.visor.school.keycloak.state.KeycloakStateReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultInitializerStateEvaluator(
    private val stateReader: KeycloakStateReader
) : InitializerStateEvaluator {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun evaluate(blueprint: KeycloakBlueprint): InitializationDecision {
        val realmName = blueprint.realm.name
        val state = stateReader.fetchRealmState(realmName)

        // Only provision if realm doesn't exist
        if (!state.exists) {
            log.info("Keycloak realm '{}' does not exist; provisioning required", realmName)
            return InitializationDecision(performProvisioning = true, reason = "Realm missing")
        }

        // Realm exists - skip provisioning
        log.info("Keycloak realm '{}' already exists; skipping provisioning", realmName)
        return InitializationDecision(performProvisioning = false, reason = "Realm already exists")
    }
}
