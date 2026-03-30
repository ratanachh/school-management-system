package com.visor.school.keycloak.state;

@FunctionalInterface
public interface KeycloakStateReader {
    RealmState fetchRealmState(String realmName);
}
