package com.visor.school.keycloak.model

/**
 * Describes the desired state of a Keycloak realm, including realm-level roles,
 * client definitions, client roles, and composite mappings between them.
 */
data class KeycloakBlueprint(
    val realm: RealmBlueprint,
    val clients: List<ClientBlueprint> = emptyList(),
    val realmRoles: List<RoleBlueprint> = emptyList(),
    val clientRoles: List<ClientRoleBlueprint> = emptyList(),
    val composites: List<RoleCompositeMapping> = emptyList()
)

data class RealmBlueprint(
    val name: String,
    val enabled: Boolean = true,
    val attributes: Map<String, String> = emptyMap()
)

data class ClientBlueprint(
    val clientId: String,
    val protocol: String = "openid-connect",
    val publicClient: Boolean = false,
    val serviceAccountsEnabled: Boolean = true,
    val redirectUris: List<String> = emptyList(),
    val webOrigins: List<String> = emptyList(),
    val attributes: Map<String, String> = emptyMap()
)

data class RoleBlueprint(
    val name: String,
    val description: String? = null
)

data class ClientRoleBlueprint(
    val clientId: String,
    val name: String,
    val description: String? = null
)

data class RoleCompositeMapping(
    val realmRole: String,
    val clientId: String,
    val clientRoles: List<String>
)

/**
 * Result of evaluating and potentially executing the initializer.
 */
data class InitializationOutcome internal constructor(
    val status: InitializationStatus,
    val message: String
) {
    companion object {
        fun skipped(reason: String): InitializationOutcome =
            InitializationOutcome(InitializationStatus.SKIPPED, reason)

        fun applied(message: String = "Keycloak configuration applied"): InitializationOutcome =
            InitializationOutcome(InitializationStatus.APPLIED, message)
    }
}

enum class InitializationStatus {
    APPLIED,
    SKIPPED
}

/**
 * Decision produced by the state evaluator describing whether provisioning should occur.
 */
data class InitializationDecision(
    val performProvisioning: Boolean,
    val reason: String? = null
)
