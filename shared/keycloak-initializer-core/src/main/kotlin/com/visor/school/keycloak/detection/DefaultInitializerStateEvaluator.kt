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

        if (!state.exists) {
            log.info("Keycloak realm '{}' does not exist; provisioning required", realmName)
            return InitializationDecision(performProvisioning = true, reason = "Realm missing")
        }

        if (!state.initializedFlag) {
            log.info("Keycloak realm '{}' missing initialization flag; provisioning required", realmName)
            return InitializationDecision(performProvisioning = true, reason = "Initialization flag not set")
        }

        val missingRealmRoles = blueprint.realmRoles.map { it.name }.filterNot(state.realmRoles::contains)
        if (missingRealmRoles.isNotEmpty()) {
            log.info(
                "Keycloak realm '{}' missing {} realm role(s); provisioning required",
                realmName,
                missingRealmRoles.size
            )
            return InitializationDecision(
                performProvisioning = true,
                reason = "Realm roles missing: ${missingRealmRoles.joinToString()}"
            )
        }

        val missingClientRoles = blueprint.clientRoles.filter { role ->
            val existingRoles = state.clientRoles[role.clientId].orEmpty()
            !existingRoles.contains(role.name)
        }
        if (missingClientRoles.isNotEmpty()) {
            log.info(
                "Keycloak realm '{}' missing {} client role(s); provisioning required",
                realmName,
                missingClientRoles.size
            )
            return InitializationDecision(
                performProvisioning = true,
                reason = "Client roles missing for ${missingClientRoles.first().clientId}"
            )
        }

        val missingComposites = blueprint.composites.filter { composite ->
            val clientRoleAssignments = state.composites[composite.realmRole].orEmpty()
            val assignedRoles = clientRoleAssignments[composite.clientId].orEmpty()
            !assignedRoles.containsAll(composite.clientRoles.toSet())
        }
        if (missingComposites.isNotEmpty()) {
            log.info(
                "Keycloak realm '{}' missing {} composite mapping(s); provisioning required",
                realmName,
                missingComposites.size
            )
            return InitializationDecision(
                performProvisioning = true,
                reason = "Role composites incomplete"
            )
        }

        log.info("Keycloak realm '{}' already satisfies blueprint; skipping provisioning", realmName)
        return InitializationDecision(performProvisioning = false, reason = "Realm initialized")
    }
}
