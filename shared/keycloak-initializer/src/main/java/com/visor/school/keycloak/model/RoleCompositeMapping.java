package com.visor.school.keycloak.model;

import java.util.Collections;
import java.util.List;

public record RoleCompositeMapping(
    String realmRole,
    String clientId,
    List<String> clientRoles
) {
    public RoleCompositeMapping {
        if (clientRoles == null) clientRoles = Collections.emptyList();
    }
}
