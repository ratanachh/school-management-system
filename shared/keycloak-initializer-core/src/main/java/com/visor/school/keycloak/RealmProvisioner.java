package com.visor.school.keycloak;

import com.visor.school.keycloak.model.InitializationOutcome;
import com.visor.school.keycloak.model.KeycloakBlueprint;

@FunctionalInterface
public interface RealmProvisioner {
    InitializationOutcome applyBlueprint(KeycloakBlueprint blueprint);
}
