package com.visor.school.keycloak.model;

public record RoleBlueprint(
    String name,
    String description
) {
    public RoleBlueprint(String name) {
        this(name, null);
    }
}
