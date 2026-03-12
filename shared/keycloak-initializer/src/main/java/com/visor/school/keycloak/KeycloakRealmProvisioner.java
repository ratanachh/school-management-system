package com.visor.school.keycloak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.visor.school.keycloak.client.KeycloakAdminClientFactory;
import com.visor.school.keycloak.model.ClientBlueprint;
import com.visor.school.keycloak.model.ClientRoleBlueprint;
import com.visor.school.keycloak.model.InitializationOutcome;
import com.visor.school.keycloak.model.KeycloakBlueprint;
import com.visor.school.keycloak.model.RoleBlueprint;
import com.visor.school.keycloak.model.RoleCompositeMapping;

import jakarta.ws.rs.NotFoundException;

@Service
public class KeycloakRealmProvisioner implements RealmProvisioner {

    private static final String INITIALIZED_FLAG_KEY = "sso.system.initialized";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final KeycloakAdminClientFactory clientFactory;

    public KeycloakRealmProvisioner(KeycloakAdminClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public InitializationOutcome applyBlueprint(KeycloakBlueprint blueprint) {
        return clientFactory.withAdminClient(keycloak -> {
            String realmName = blueprint.realm().name();
            RealmsResource realms = keycloak.realms();
            RealmResource realmResource = ensureRealm(realms, blueprint);

            log.debug("Blueprint has {} clients, {} realm roles, {} client roles, {} composites", 
                blueprint.clients().size(), blueprint.realmRoles().size(), blueprint.clientRoles().size(), blueprint.composites().size());

            ensureClients(realmResource, blueprint.clients());
            ensureRealmRoles(realmResource, blueprint);
            ensureClientRoles(realmResource, blueprint);
            ensureCompositeMappings(realmResource, blueprint.composites());
            ensureInitializationFlag(realmResource);

            return InitializationOutcome.applied("Keycloak realm '" + realmName + "' provisioned successfully");
        });
    }

    private RealmResource ensureRealm(RealmsResource realms, KeycloakBlueprint blueprint) {
        String realmName = blueprint.realm().name();
        RealmResource realmResource = realms.realm(realmName);
        RealmRepresentation representation;
        try {
            representation = realmResource.toRepresentation();
        } catch (NotFoundException notFound) {
            RealmRepresentation newRealm = new RealmRepresentation();
            newRealm.setId(realmName);
            newRealm.setRealm(realmName);
            newRealm.setEnabled(blueprint.realm().enabled());
            
            // Don't set the initialization flag yet - it will be set after all provisioning is complete
            Map<String, String> attributes = new HashMap<>(blueprint.realm().attributes());
            attributes.remove(INITIALIZED_FLAG_KEY);
            newRealm.setAttributes(attributes);

            realms.create(newRealm);
            log.info("Created Keycloak realm '{}'", realmName);
            return realms.realm(realmName);
        }

        boolean updated = false;
        if (representation.isEnabled() != blueprint.realm().enabled()) {
            representation.setEnabled(blueprint.realm().enabled());
            updated = true;
        }

        Map<String, String> attributes = representation.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        
        for (Map.Entry<String, String> entry : blueprint.realm().attributes().entrySet()) {
            if (!Objects.equals(attributes.get(entry.getKey()), entry.getValue())) {
                attributes.put(entry.getKey(), entry.getValue());
                updated = true;
            }
        }
        
        if (!Objects.equals(attributes, representation.getAttributes())) {
            representation.setAttributes(attributes);
        }

        if (updated) {
            realmResource.update(representation);
            log.info("Updated Keycloak realm '{}' attributes", realmName);
        }
        return realmResource;
    }

    private void ensureClients(RealmResource realmResource, List<ClientBlueprint> clients) {
        log.info("ensureClients called with {} clients", clients.size());
        ClientsResource clientsResource = realmResource.clients();
        
        for (ClientBlueprint clientBlueprint : clients) {
            List<ClientRepresentation> existingList = clientsResource.findByClientId(clientBlueprint.clientId());
            ClientRepresentation existing = existingList.isEmpty() ? null : existingList.get(0);

            if (existing == null) {
                ClientRepresentation newClient = new ClientRepresentation();
                newClient.setClientId(clientBlueprint.clientId());
                newClient.setProtocol(clientBlueprint.protocol());
                newClient.setPublicClient(clientBlueprint.publicClient());
                newClient.setServiceAccountsEnabled(clientBlueprint.serviceAccountsEnabled());
                newClient.setRedirectUris(clientBlueprint.redirectUris());
                newClient.setWebOrigins(clientBlueprint.webOrigins());
                newClient.setAttributes(new HashMap<>(clientBlueprint.attributes()));
                newClient.setSecret(clientBlueprint.secret());

                clientsResource.create(newClient);
                log.info("Created Keycloak client '{}'", clientBlueprint.clientId());

                // If this is a service account client, assign realm-management roles
                if (clientBlueprint.serviceAccountsEnabled()) {
                    assignServiceAccountRoles(realmResource, clientBlueprint.clientId());
                }
            } else {
                ClientResource clientResource = clientsResource.get(existing.getId());
                boolean updated = synchronizeClientRepresentation(existing, clientBlueprint);

                // Update secret if provided
                if (clientBlueprint.secret() != null && !Objects.equals(existing.getSecret(), clientBlueprint.secret())) {
                    existing.setSecret(clientBlueprint.secret());
                    clientResource.update(existing);
                    log.info("Updated secret for Keycloak client '{}'", clientBlueprint.clientId());
                } else if (updated) {
                    clientResource.update(existing);
                    log.info("Updated Keycloak client '{}'", clientBlueprint.clientId());
                }
            }
        }
    }

    private boolean synchronizeClientRepresentation(ClientRepresentation existing, ClientBlueprint blueprint) {
        boolean updated = false;
        
        if (!Objects.equals(existing.getProtocol(), blueprint.protocol())) {
            existing.setProtocol(blueprint.protocol());
            updated = true;
        }
        if (!Objects.equals(existing.isPublicClient(), blueprint.publicClient())) {
            existing.setPublicClient(blueprint.publicClient());
            updated = true;
        }
        if (!Objects.equals(existing.isServiceAccountsEnabled(), blueprint.serviceAccountsEnabled())) {
            existing.setServiceAccountsEnabled(blueprint.serviceAccountsEnabled());
            updated = true;
        }

        Set<String> existingRedirectUris = existing.getRedirectUris() != null ? new HashSet<>(existing.getRedirectUris()) : Collections.emptySet();
        if (!existingRedirectUris.equals(new HashSet<>(blueprint.redirectUris()))) {
            existing.setRedirectUris(blueprint.redirectUris());
            updated = true;
        }

        Set<String> existingWebOrigins = existing.getWebOrigins() != null ? new HashSet<>(existing.getWebOrigins()) : Collections.emptySet();
        if (!existingWebOrigins.equals(new HashSet<>(blueprint.webOrigins()))) {
            existing.setWebOrigins(blueprint.webOrigins());
            updated = true;
        }

        Map<String, String> attributes = existing.getAttributes() != null ? new HashMap<>(existing.getAttributes()) : new HashMap<>();
        for (Map.Entry<String, String> entry : blueprint.attributes().entrySet()) {
            if (!Objects.equals(attributes.get(entry.getKey()), entry.getValue())) {
                attributes.put(entry.getKey(), entry.getValue());
                updated = true;
            }
        }
        
        if (!Objects.equals(attributes, existing.getAttributes())) {
            existing.setAttributes(attributes);
        }

        return updated;
    }

    private void ensureRealmRoles(RealmResource realmResource, KeycloakBlueprint blueprint) {
        RolesResource rolesResource = realmResource.roles();
        for (RoleBlueprint role : blueprint.realmRoles()) {
            try {
                RoleResource roleResource = rolesResource.get(role.name());
                RoleRepresentation existing = roleResource.toRepresentation();
                
                if (role.description() != null && !Objects.equals(role.description(), existing.getDescription())) {
                    existing.setDescription(role.description());
                    roleResource.update(existing);
                    log.info("Updated description for realm role '{}'", role.name());
                }
            } catch (NotFoundException notFound) {
                RoleRepresentation representation = new RoleRepresentation();
                representation.setName(role.name());
                representation.setDescription(role.description());
                
                rolesResource.create(representation);
                log.info("Created realm role '{}'", role.name());
            }
        }
    }

    private void ensureClientRoles(RealmResource realmResource, KeycloakBlueprint blueprint) {
        ClientsResource clientsResource = realmResource.clients();
        
        // Group client roles by client ID
        Map<String, List<ClientRoleBlueprint>> rolesByClient = blueprint.clientRoles().stream()
            .collect(Collectors.groupingBy(ClientRoleBlueprint::clientId));

        rolesByClient.forEach((clientId, roles) -> {
            List<ClientRepresentation> clientRepresentations = clientsResource.findByClientId(clientId);
            if (clientRepresentations.isEmpty()) {
                log.warn("Client '{}' not found when assigning client roles", clientId);
                return;
            }
            ClientRepresentation clientRepresentation = clientRepresentations.get(0);
            ClientResource clientResource = clientsResource.get(clientRepresentation.getId());

            for (ClientRoleBlueprint role : roles) {
                try {
                    RoleResource roleResource = clientResource.roles().get(role.name());
                    RoleRepresentation existing = roleResource.toRepresentation();
                    
                    if (role.description() != null && !Objects.equals(role.description(), existing.getDescription())) {
                        existing.setDescription(role.description());
                        roleResource.update(existing);
                        log.info("Updated client role '{}' for client '{}'", role.name(), clientId);
                    }
                } catch (NotFoundException notFound) {
                    RoleRepresentation representation = new RoleRepresentation();
                    representation.setName(role.name());
                    representation.setDescription(role.description());
                    representation.setClientRole(true);
                    representation.setContainerId(clientRepresentation.getId());
                    
                    clientResource.roles().create(representation);
                    log.info("Created client role '{}' for client '{}'", role.name(), clientId);
                }
            }
        });
    }

    private void ensureCompositeMappings(RealmResource realmResource, List<RoleCompositeMapping> composites) {
        if (composites.isEmpty()) return;
        
        ClientsResource clientsResource = realmResource.clients();
        List<ClientRepresentation> clients = clientsResource.findAll();
        Map<String, ClientRepresentation> clientsByClientId = clients.stream()
            .collect(Collectors.toMap(ClientRepresentation::getClientId, c -> c));

        for (RoleCompositeMapping composite : composites) {
            RoleResource realmRoleResource;
            try {
                realmRoleResource = realmResource.roles().get(composite.realmRole());
                // Verify existence
                realmRoleResource.toRepresentation();
            } catch (NotFoundException notFound) {
                log.warn("Skipping composite mapping for realm role '{}' because it does not exist", composite.realmRole());
                continue;
            }

            ClientRepresentation clientRepresentation = clientsByClientId.get(composite.clientId());
            if (clientRepresentation == null) {
                log.warn(
                    "Skipping composite mapping for realm role '{}' because client '{}' is missing",
                    composite.realmRole(),
                    composite.clientId()
                );
                continue;
            }

            ClientResource clientResource = clientsResource.get(clientRepresentation.getId());
            Set<String> existingComposites = Collections.emptySet();
            
            var roleComposites = realmRoleResource.toRepresentation().getComposites();
            if (roleComposites != null && roleComposites.getClient() != null) {
                List<String> clientRolesList = roleComposites.getClient().get(clientRepresentation.getClientId());
                if (clientRolesList != null) {
                    existingComposites = new HashSet<>(clientRolesList);
                }
            }

            Set<String> finalExistingComposites = existingComposites;
            List<String> missing = composite.clientRoles().stream()
                .filter(role -> !finalExistingComposites.contains(role))
                .toList();

            if (missing.isEmpty()) {
                continue;
            }

            List<RoleRepresentation> roleRepresentations = missing.stream()
                .map(roleName -> clientResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());
                
            realmRoleResource.addComposites(roleRepresentations);
            log.info(
                "Added {} composite role(s) to realm role '{}' for client '{}'",
                roleRepresentations.size(),
                composite.realmRole(),
                composite.clientId()
            );
        }
    }

    private void ensureInitializationFlag(RealmResource realmResource) {
        RealmRepresentation representation = realmResource.toRepresentation();
        Map<String, String> attributes = representation.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        
        String currentValue = attributes.get(INITIALIZED_FLAG_KEY);
        if ("true".equalsIgnoreCase(currentValue)) {
            return;
        }
        
        attributes.put(INITIALIZED_FLAG_KEY, "true");
        representation.setAttributes(attributes);
        realmResource.update(representation);
        log.info("Marked realm '{}' as initialized", representation.getRealm());
    }

    private void assignServiceAccountRoles(RealmResource realmResource, String clientId) {
        try {
            ClientsResource clientsResource = realmResource.clients();
            List<ClientRepresentation> clientList = clientsResource.findByClientId(clientId);
            if (clientList.isEmpty()) {
                log.warn("Client '{}' not found when assigning service account roles", clientId);
                return;
            }
            ClientRepresentation clientRepresentation = clientList.get(0);

            // Get the service account user for this client
            UserRepresentation serviceAccountUser = clientsResource.get(clientRepresentation.getId()).getServiceAccountUser();
            if (serviceAccountUser == null) {
                log.warn("Service account user not found for client '{}'", clientId);
                return;
            }

            // Get the realm-management client
            List<ClientRepresentation> realmManagementList = clientsResource.findByClientId("realm-management");
            if (realmManagementList.isEmpty()) {
                log.warn("realm-management client not found");
                return;
            }
            ClientRepresentation realmManagementClient = realmManagementList.get(0);

            // Get the manage-users and view-users roles from realm-management
            RolesResource realmManagementRoles = clientsResource.get(realmManagementClient.getId()).roles();
            List<RoleRepresentation> rolesToAssign = new ArrayList<>();
            
            try {
                rolesToAssign.add(realmManagementRoles.get("manage-users").toRepresentation());
            } catch (Exception e) {
                log.warn("manage-users role not found in realm-management");
            }
            
            try {
                rolesToAssign.add(realmManagementRoles.get("view-users").toRepresentation());
            } catch (Exception e) {
                log.warn("view-users role not found in realm-management");
            }

            if (!rolesToAssign.isEmpty()) {
                // Assign the roles to the service account user
                UserResource userResource = realmResource.users().get(serviceAccountUser.getId());
                userResource.roles().clientLevel(realmManagementClient.getId()).add(rolesToAssign);
                log.info("Assigned {} realm-management roles to service account for client '{}'", rolesToAssign.size(), clientId);
            }
        } catch (Exception e) {
            log.error("Failed to assign service account roles for client '{}'", clientId, e);
        }
    }
}
