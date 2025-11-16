package com.visor.school.keycloak.state

import com.visor.school.keycloak.client.KeycloakAdminClientFactory
import jakarta.ws.rs.NotFoundException
import org.keycloak.representations.idm.ClientRepresentation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KeycloakAdminStateReader(
    private val clientFactory: KeycloakAdminClientFactory
) : KeycloakStateReader {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun fetchRealmState(realmName: String): RealmState =
        clientFactory.withAdminClient { keycloak ->
            val realms = keycloak.realms()
            val realmResource = try {
                realms.realm(realmName).also { it.toRepresentation() }
            } catch (notFound: NotFoundException) {
                log.info("Realm '{}' does not exist in Keycloak", realmName)
                return@withAdminClient RealmState(exists = false, initializedFlag = false)
            }

            val realmRepresentation = realmResource.toRepresentation()
            val initializedFlag = realmRepresentation.attributes
                ?.get(INITIALIZED_FLAG_KEY)
                ?.equals("true", ignoreCase = true)
                ?: false

            val realmRoles = realmResource.roles().list().mapTo(mutableSetOf()) { it.name }

            val clients = realmResource.clients().findAll()
            val clientsById = clients.associateBy(ClientRepresentation::getId)
            val clientRoles = clients.associate { client ->
                client.clientId to realmResource.clients().get(client.id).roles().list().map { it.name }.toSet()
            }

            val composites = realmResource.roles().list().associate { role ->
                val realmRoleResource = realmResource.roles().get(role.name)
                role.name to clientCompositeMappings(realmRoleResource, clientsById)
            }

            RealmState(
                exists = true,
                initializedFlag = initializedFlag,
                realmRoles = realmRoles,
                clientRoles = clientRoles,
                composites = composites
            )
        }

    private fun clientCompositeMappings(
        realmRoleResource: org.keycloak.admin.client.resource.RoleResource,
        clientsById: Map<String, ClientRepresentation>
    ): Map<String, Set<String>> {
        val clientComposites = realmRoleResource.toRepresentation().composites?.client ?: emptyMap()
        return clientComposites.mapNotNull { (clientId, roles) ->
            val client = clientsById[clientId] ?: return@mapNotNull null
            client.clientId to roles.toSet()
        }.toMap()
    }

    companion object {
        private const val INITIALIZED_FLAG_KEY = "sso.system.initialized"
    }
}
