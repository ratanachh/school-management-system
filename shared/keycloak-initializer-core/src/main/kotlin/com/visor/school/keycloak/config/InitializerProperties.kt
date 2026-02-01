package com.visor.school.keycloak.config

import com.visor.school.keycloak.model.ClientBlueprint
import com.visor.school.keycloak.model.ClientRoleBlueprint
import com.visor.school.keycloak.model.KeycloakBlueprint
import com.visor.school.keycloak.model.RealmBlueprint
import com.visor.school.keycloak.model.RoleBlueprint
import com.visor.school.keycloak.model.RoleCompositeMapping
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak.initializer")
data class InitializerProperties(
    val enabled: Boolean = true,
    val admin: Admin = Admin(),
    val realm: Realm = Realm(),
    val clients: List<Client> = emptyList(),
    val realmRoles: List<Role> = emptyList(),
    val clientRoles: List<ClientRole> = emptyList(),
    val composites: List<Composite> = emptyList(),
    val retry: Retry = Retry()
) {

    fun toBlueprint(): KeycloakBlueprint = KeycloakBlueprint(
        realm = RealmBlueprint(
            name = realm.name,
            enabled = realm.enabled,
            attributes = realm.attributes
        ),
        clients = clients.map { client ->
            ClientBlueprint(
                clientId = client.clientId,
                protocol = client.protocol,
                publicClient = client.publicClient,
                serviceAccountsEnabled = client.serviceAccountsEnabled,
                redirectUris = client.redirectUris,
                webOrigins = client.webOrigins,
                attributes = client.attributes,
                secret = client.secret
            )
        },
        realmRoles = realmRoles.map { role ->
            RoleBlueprint(name = role.name, description = role.description)
        },
        clientRoles = clientRoles.map { role ->
            ClientRoleBlueprint(
                clientId = role.clientId,
                name = role.name,
                description = role.description
            )
        },
        composites = composites.map { composite ->
            RoleCompositeMapping(
                realmRole = composite.realmRole,
                clientId = composite.clientId,
                clientRoles = composite.clientRoles
            )
        }
    )

    data class Admin(
        val url: String = "http://localhost:8080",
        val username: String = "admin",
        val password: String = "admin",
        val clientId: String = "admin-cli"
    )

    data class Realm(
        val name: String = "school-management",
        val enabled: Boolean = true,
        val attributes: Map<String, String> = mapOf("sso.system.initialized" to "true")
    )

    data class Client(
        val clientId: String,
        val protocol: String = "openid-connect",
        val publicClient: Boolean = false,
        val serviceAccountsEnabled: Boolean = true,
        val redirectUris: List<String> = emptyList(),
        val webOrigins: List<String> = emptyList(),
        val attributes: Map<String, String> = emptyMap(),
        val secret: String? = null
    )

    data class Role(
        val name: String,
        val description: String? = null
    )

    data class ClientRole(
        val clientId: String,
        val name: String,
        val description: String? = null
    )

    data class Composite(
        val realmRole: String,
        val clientId: String,
        val clientRoles: List<String>
    )

    data class Retry(
        val maxAttempts: Int = 5,
        val initialBackoffMillis: Long = 2_000L,
        val multiplier: Double = 1.5,
        val failOnError: Boolean = false
    )
}
