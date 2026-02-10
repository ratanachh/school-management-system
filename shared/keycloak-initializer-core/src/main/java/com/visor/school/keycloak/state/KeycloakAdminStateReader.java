package com.visor.school.keycloak.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visor.school.keycloak.client.KeycloakAdminClientFactory;

import jakarta.ws.rs.NotFoundException;

@Component
public class KeycloakAdminStateReader implements KeycloakStateReader {

    private static final String INITIALIZED_FLAG_KEY = "sso.system.initialized";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final KeycloakAdminClientFactory clientFactory;

    public KeycloakAdminStateReader(KeycloakAdminClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public RealmState fetchRealmState(String realmName) {
        return clientFactory.withAdminClient(keycloak -> {
            RealmResource realmResource;
            try {
                // Check if realm exists by trying to get representation
                keycloak.realm(realmName).toRepresentation();
                realmResource = keycloak.realm(realmName);
            } catch (NotFoundException notFound) {
                log.info("Realm '{}' does not exist in Keycloak", realmName);
                return new RealmState(false, false);
            }

            RealmRepresentation realmRepresentation = realmResource.toRepresentation();
            boolean initializedFlag = false;
            if (realmRepresentation.getAttributes() != null) {
                String initializedValue = realmRepresentation.getAttributes().get(INITIALIZED_FLAG_KEY);
                initializedFlag = "true".equalsIgnoreCase(initializedValue);
            }

            Set<String> realmRoles = realmResource.roles().list().stream()
                .map(org.keycloak.representations.idm.RoleRepresentation::getName)
                .collect(Collectors.toSet());

            List<ClientRepresentation> clients = realmResource.clients().findAll();
            Map<String, ClientRepresentation> clientsById = clients.stream()
                .collect(Collectors.toMap(ClientRepresentation::getId, c -> c));

            Map<String, Set<String>> clientRoles = clients.stream().collect(Collectors.toMap(
                ClientRepresentation::getClientId,
                client -> realmResource.clients().get(client.getId()).roles().list().stream()
                    .map(org.keycloak.representations.idm.RoleRepresentation::getName)
                    .collect(Collectors.toSet())
            ));

            Map<String, Map<String, Set<String>>> composites = realmResource.roles().list().stream().collect(Collectors.toMap(
                org.keycloak.representations.idm.RoleRepresentation::getName,
                role -> {
                    var realmRoleResource = realmResource.roles().get(role.getName());
                    return clientCompositeMappings(realmRoleResource, clientsById);
                }
            ));

            return new RealmState(
                true,
                initializedFlag,
                realmRoles,
                clientRoles,
                composites
            );
        });
    }

    private Map<String, Set<String>> clientCompositeMappings(
        org.keycloak.admin.client.resource.RoleResource realmRoleResource,
        Map<String, ClientRepresentation> clientsById
    ) {
        var composites = realmRoleResource.toRepresentation().getComposites();
        if (composites == null || composites.getClient() == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> clientComposites = composites.getClient();
        
        Map<String, Set<String>> result = new HashMap<>();
        clientComposites.forEach((clientId, roles) -> {
            ClientRepresentation client = clientsById.get(clientId);
            if (client != null) {
                result.put(client.getClientId(), Set.copyOf(roles));
            }
        });
        return result;
    }
}
