package com.visor.school.keycloak

import com.visor.school.keycloak.detection.InitializerStateEvaluator
import com.visor.school.keycloak.model.InitializationOutcome
import com.visor.school.keycloak.model.KeycloakBlueprint
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InitializerService(
    private val stateEvaluator: InitializerStateEvaluator,
    private val realmProvisioner: RealmProvisioner
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun initialize(blueprint: KeycloakBlueprint): InitializationOutcome {
        val decision = stateEvaluator.evaluate(blueprint)
        if (!decision.performProvisioning) {
            val reason = decision.reason ?: "Keycloak already initialized"
            log.info("Keycloak initializer skipped: {}", reason)
            return InitializationOutcome.skipped(reason)
        }

        log.info("Applying Keycloak initializer blueprint for realm '{}'", blueprint.realm.name)
        return realmProvisioner.applyBlueprint(blueprint)
    }
}

fun interface RealmProvisioner {
    fun applyBlueprint(blueprint: KeycloakBlueprint): InitializationOutcome
}
