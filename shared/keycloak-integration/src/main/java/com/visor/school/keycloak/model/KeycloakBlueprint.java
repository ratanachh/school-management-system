package com.visor.school.keycloak.model;

import java.util.Collections;
import java.util.List;

/**
 * Describes the desired state of a Keycloak realm, including realm-level roles,
 * client definitions, client roles, and composite mappings between them.
 */
public record KeycloakBlueprint(
    RealmBlueprint realm,
    List<ClientBlueprint> clients,
    List<RoleBlueprint> realmRoles,
    List<ClientRoleBlueprint> clientRoles,
    List<RoleCompositeMapping> composites
) {
    public KeycloakBlueprint {
        if (clients == null) clients = Collections.emptyList();
        if (realmRoles == null) realmRoles = Collections.emptyList();
        if (clientRoles == null) clientRoles = Collections.emptyList();
        if (composites == null) composites = Collections.emptyList();
    }
}
