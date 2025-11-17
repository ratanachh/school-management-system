package com.visor.school.keycloak.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider
import com.visor.school.keycloak.config.InitializerProperties
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl
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
        
        // Configure ObjectMapper to ignore unknown properties from newer Keycloak versions
        val objectMapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        
        val resteasyClient = ResteasyClientBuilderImpl()
            .register(JacksonJsonProvider(objectMapper))
            .build()
        
        KeycloakBuilder.builder()
            .serverUrl(admin.url)
            .realm(ADMIN_REALM)
            .clientId(admin.clientId)
            .username(admin.username)
            .password(admin.password)
            .grantType(OAuth2Constants.PASSWORD)
            .resteasyClient(resteasyClient)
            .build()
            .use { client -> 
                try {
                    return action(client)
                } finally {
                    resteasyClient.close()
                }
            }
    }

    companion object {
        private const val ADMIN_REALM = "master"
    }
}

