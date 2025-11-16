package com.visor.school.keycloak.state

/**
 * Snapshot of the existing Keycloak realm used to determine whether provisioning is required.
 */
data class RealmState(
    val exists: Boolean,
    val initializedFlag: Boolean,
    val realmRoles: Set<String> = emptySet(),
    val clientRoles: Map<String, Set<String>> = emptyMap(),
    val composites: Map<String, Map<String, Set<String>>> = emptyMap()
)

fun interface KeycloakStateReader {
    fun fetchRealmState(realmName: String): RealmState
}
