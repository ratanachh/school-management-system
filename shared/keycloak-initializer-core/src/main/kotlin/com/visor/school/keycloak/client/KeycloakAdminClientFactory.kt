package com.visor.school.keycloak.client

import com.visor.school.keycloak.config.InitializerProperties
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.stereotype.Component

/**
 * Factory that produces short-lived Keycloak admin clients for performing provisioning operations.
 * Each invocation creates a new client instance to avoid stale tokens and ensure thread-safety.
 */
@Component
class KeycloakAdminClientFactory(
    private val properties: InitializerProperties
) {

    fun <T> withAdminClient(action: (Keycloak) -> T): T {
        val admin = properties.admin
        KeycloakBuilder.builder()
            .serverUrl(admin.url)
            .realm(ADMIN_REALM)
            .clientId(admin.clientId)
            .username(admin.username)
            .password(admin.password)
            .grantType(OAuth2Constants.PASSWORD)
            .build()
            .use { client -> return action(client) }
    }

    companion object {
        private const val ADMIN_REALM = "master"
    }
}

