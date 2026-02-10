package com.visor.school.keycloak.model;

public record ClientRoleBlueprint(
    String clientId,
    String name,
    String description
) {
    public ClientRoleBlueprint(String clientId, String name) {
        this(clientId, name, null);
    }
}
