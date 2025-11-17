package com.visor.school.keycloak

import com.visor.school.keycloak.client.KeycloakAdminClientFactory
import com.visor.school.keycloak.model.ClientBlueprint
import com.visor.school.keycloak.model.InitializationOutcome
import com.visor.school.keycloak.model.KeycloakBlueprint
import com.visor.school.keycloak.model.RoleCompositeMapping
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class KeycloakRealmProvisioner(
    private val clientFactory: KeycloakAdminClientFactory
) : RealmProvisioner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun applyBlueprint(blueprint: KeycloakBlueprint): InitializationOutcome =
        clientFactory.withAdminClient { keycloak ->
            val realmName = blueprint.realm.name
            val realms = keycloak.realms()
            val realmResource = ensureRealm(realms, blueprint)

            log.debug("Blueprint has {} clients, {} realm roles, {} client roles, {} composites", 
                blueprint.clients.size, blueprint.realmRoles.size, blueprint.clientRoles.size, blueprint.composites.size)
            
            ensureClients(realmResource, blueprint.clients)
            ensureRealmRoles(realmResource, blueprint)
            ensureClientRoles(realmResource, blueprint)
            ensureCompositeMappings(realmResource, blueprint.composites)
            ensureInitializationFlag(realmResource)

            InitializationOutcome.applied("Keycloak realm '$realmName' provisioned successfully")
        }

    private fun ensureRealm(realms: org.keycloak.admin.client.resource.RealmsResource, blueprint: KeycloakBlueprint): RealmResource {
        val realmName = blueprint.realm.name
        val realmResource = realms.realm(realmName)
        val representation = try {
            realmResource.toRepresentation()
        } catch (notFound: NotFoundException) {
            val newRealm = RealmRepresentation().apply {
                id = realmName
                realm = realmName
                isEnabled = blueprint.realm.enabled
                // Don't set the initialization flag yet - it will be set after all provisioning is complete
                attributes = blueprint.realm.attributes.filterKeys { it != INITIALIZED_FLAG_KEY }.toMutableMap()
            }
            realms.create(newRealm)
            log.info("Created Keycloak realm '{}'", realmName)
            return realms.realm(realmName)
        }

        var updated = false
        if (representation.isEnabled != blueprint.realm.enabled) {
            representation.isEnabled = blueprint.realm.enabled
            updated = true
        }

        val attributes = representation.attributes ?: mutableMapOf()
        blueprint.realm.attributes.forEach { (key, value) ->
            if (attributes[key] != value) {
                attributes[key] = value
                updated = true
            }
        }
        representation.attributes = attributes

        if (updated) {
            realmResource.update(representation)
            log.info("Updated Keycloak realm '{}' attributes", realmName)
        }
        return realmResource
    }

    private fun ensureClients(realmResource: RealmResource, clients: List<ClientBlueprint>) {
        log.info("ensureClients called with {} clients", clients.size)
        val clientsResource = realmResource.clients()
        clients.forEach { clientBlueprint ->
            val existing = clientsResource.findByClientId(clientBlueprint.clientId).firstOrNull()
            if (existing == null) {
                val newClient = ClientRepresentation().apply {
                    clientId = clientBlueprint.clientId
                    protocol = clientBlueprint.protocol
                    isPublicClient = clientBlueprint.publicClient
                    isServiceAccountsEnabled = clientBlueprint.serviceAccountsEnabled
                    redirectUris = clientBlueprint.redirectUris
                    webOrigins = clientBlueprint.webOrigins
                    attributes = clientBlueprint.attributes.toMutableMap()
                }
                clientsResource.create(newClient)
                log.info("Created Keycloak client '{}'", clientBlueprint.clientId)
            } else {
                val clientResource = clientsResource.get(existing.id)
                val updated = synchronizeClientRepresentation(existing, clientBlueprint)
                if (updated) {
                    clientResource.update(existing)
                    log.info("Updated Keycloak client '{}'", clientBlueprint.clientId)
                }
            }
        }
    }

    private fun synchronizeClientRepresentation(existing: ClientRepresentation, blueprint: ClientBlueprint): Boolean {
        var updated = false
        if (existing.protocol != blueprint.protocol) {
            existing.protocol = blueprint.protocol
            updated = true
        }
        if (existing.isPublicClient != blueprint.publicClient) {
            existing.isPublicClient = blueprint.publicClient
            updated = true
        }
        if (existing.isServiceAccountsEnabled != blueprint.serviceAccountsEnabled) {
            existing.isServiceAccountsEnabled = blueprint.serviceAccountsEnabled
            updated = true
        }

        val redirectUris = existing.redirectUris?.toSet() ?: emptySet()
        if (redirectUris != blueprint.redirectUris.toSet()) {
            existing.redirectUris = blueprint.redirectUris
            updated = true
        }

        val webOrigins = existing.webOrigins?.toSet() ?: emptySet()
        if (webOrigins != blueprint.webOrigins.toSet()) {
            existing.webOrigins = blueprint.webOrigins
            updated = true
        }

        val attributes = existing.attributes?.toMutableMap() ?: mutableMapOf()
        blueprint.attributes.forEach { (key, value) ->
            if (attributes[key] != value) {
                attributes[key] = value
                updated = true
            }
        }
        if (attributes != existing.attributes) {
            existing.attributes = attributes
        }

        return updated
    }

    private fun ensureRealmRoles(realmResource: RealmResource, blueprint: KeycloakBlueprint) {
        val rolesResource = realmResource.roles()
        blueprint.realmRoles.forEach { role ->
            try {
                val existing = rolesResource.get(role.name).toRepresentation()
                if (role.description != null && role.description != existing.description) {
                    existing.description = role.description
                    rolesResource.get(role.name).update(existing)
                    log.info("Updated description for realm role '{}'", role.name)
                }
            } catch (notFound: NotFoundException) {
                val representation = RoleRepresentation().apply {
                    name = role.name
                    description = role.description
                }
                rolesResource.create(representation)
                log.info("Created realm role '{}'", role.name)
            }
        }
    }

    private fun ensureClientRoles(realmResource: RealmResource, blueprint: KeycloakBlueprint) {
        val clientsResource = realmResource.clients()
        blueprint.clientRoles
            .groupBy { it.clientId }
            .forEach { (clientId, roles) ->
                val clientRepresentation = clientsResource.findByClientId(clientId).firstOrNull()
                if (clientRepresentation == null) {
                    log.warn("Client '{}' not found when assigning client roles", clientId)
                    return@forEach
                }

                val clientResource = clientsResource.get(clientRepresentation.id)
                roles.forEach { role ->
                    try {
                        val existing = clientResource.roles().get(role.name).toRepresentation()
                        if (role.description != null && role.description != existing.description) {
                            existing.description = role.description
                            clientResource.roles().get(role.name).update(existing)
                            log.info("Updated client role '{}' for client '{}'", role.name, clientId)
                        }
                    } catch (notFound: NotFoundException) {
                        val representation = RoleRepresentation().apply {
                            name = role.name
                            description = role.description
                            clientRole = true
                            containerId = clientRepresentation.id
                        }
                        clientResource.roles().create(representation)
                        log.info("Created client role '{}' for client '{}'", role.name, clientId)
                    }
                }
            }
    }

    private fun ensureCompositeMappings(realmResource: RealmResource, composites: List<RoleCompositeMapping>) {
        if (composites.isEmpty()) return
        val clientsResource = realmResource.clients()
        val clients = clientsResource.findAll()
        val clientsByClientId = clients.associateBy(ClientRepresentation::getClientId)

        composites.forEach { composite ->
            val realmRoleResource = try {
                realmResource.roles().get(composite.realmRole)
            } catch (notFound: NotFoundException) {
                log.warn("Skipping composite mapping for realm role '{}' because it does not exist", composite.realmRole)
                return@forEach
            }

            val clientRepresentation = clientsByClientId[composite.clientId]
            if (clientRepresentation == null) {
                log.warn(
                    "Skipping composite mapping for realm role '{}' because client '{}' is missing",
                    composite.realmRole,
                    composite.clientId
                )
                return@forEach
            }

            val clientResource = clientsResource.get(clientRepresentation.id)
            val existingComposites = realmRoleResource.toRepresentation()
                .composites
                ?.client
                ?.get(clientRepresentation.id)
                ?.toSet()
                ?: emptySet()

            val missing = composite.clientRoles.filterNot(existingComposites::contains)
            if (missing.isEmpty()) {
                return@forEach
            }

            val roleRepresentations = missing.map { roleName ->
                clientResource.roles().get(roleName).toRepresentation()
            }
            realmRoleResource.addComposites(roleRepresentations)
            log.info(
                "Added {} composite role(s) to realm role '{}' for client '{}'",
                roleRepresentations.size,
                composite.realmRole,
                composite.clientId
            )
        }
    }

    private fun ensureInitializationFlag(realmResource: RealmResource) {
        val representation = realmResource.toRepresentation()
        val attributes = representation.attributes ?: mutableMapOf()
        val currentValue = attributes[INITIALIZED_FLAG_KEY]
        if (currentValue?.equals("true", ignoreCase = true) == true) {
            return
        }
        attributes[INITIALIZED_FLAG_KEY] = "true"
        representation.attributes = attributes
        realmResource.update(representation)
        log.info("Marked realm '{}' as initialized", representation.realm)
    }

    companion object {
        private const val INITIALIZED_FLAG_KEY = "sso.system.initialized"
    }
}
