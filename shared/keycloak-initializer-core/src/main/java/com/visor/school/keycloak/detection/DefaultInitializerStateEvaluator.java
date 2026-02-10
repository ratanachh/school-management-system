package com.visor.school.keycloak.detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visor.school.keycloak.model.InitializationDecision;
import com.visor.school.keycloak.model.KeycloakBlueprint;
import com.visor.school.keycloak.state.KeycloakStateReader;
import com.visor.school.keycloak.state.RealmState;

@Component
public class DefaultInitializerStateEvaluator implements InitializerStateEvaluator {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final KeycloakStateReader stateReader;

    public DefaultInitializerStateEvaluator(KeycloakStateReader stateReader) {
        this.stateReader = stateReader;
    }

    @Override
    public InitializationDecision evaluate(KeycloakBlueprint blueprint) {
        String realmName = blueprint.realm().name();
        RealmState state = stateReader.fetchRealmState(realmName);

        // Only provision if realm doesn't exist
        if (!state.exists()) {
            log.info("Keycloak realm '{}' does not exist; provisioning required", realmName);
            return new InitializationDecision(true, "Realm missing");
        }

        if (!state.initializedFlag()) {
            log.info("Keycloak realm '{}' exists but initialized flag is missing; provisioning required", realmName);
            return new InitializationDecision(true, "Realm present but not initialized");
        }

        // Realm exists - skip provisioning
        log.info("Keycloak realm '{}' already exists; skipping provisioning", realmName);
        return new InitializationDecision(false, "Realm already exists");
    }
}
