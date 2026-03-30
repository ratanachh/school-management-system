package com.visor.school.keycloak.detection;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (hasConfigurationDrift(blueprint, state)) {
            log.info("Keycloak realm '{}' exists but configuration drift was detected; provisioning required", realmName);
            return new InitializationDecision(true, "Configuration drift detected");
        }

        log.info("Keycloak realm '{}' already matches desired state; skipping provisioning", realmName);
        return new InitializationDecision(false, "Realm already reconciled");
    }

    private boolean hasConfigurationDrift(KeycloakBlueprint blueprint, RealmState state) {
        Set<String> expectedRealmRoles = blueprint.realmRoles().stream()
            .map(role -> role.name())
            .collect(Collectors.toSet());
        if (!state.realmRoles().containsAll(expectedRealmRoles)) {
            return true;
        }

        Set<String> expectedClients = blueprint.clients().stream()
            .map(client -> client.clientId())
            .collect(Collectors.toSet());
        if (!state.clientRoles().keySet().containsAll(expectedClients)) {
            return true;
        }

        Map<String, Set<String>> expectedClientRoles = blueprint.clientRoles().stream()
            .collect(Collectors.groupingBy(
                role -> role.clientId(),
                Collectors.mapping(role -> role.name(), Collectors.toSet())
            ));
        for (Map.Entry<String, Set<String>> entry : expectedClientRoles.entrySet()) {
            Set<String> actualRoles = state.clientRoles().get(entry.getKey());
            if (actualRoles == null || !actualRoles.containsAll(entry.getValue())) {
                return true;
            }
        }

        for (var composite : blueprint.composites()) {
            Map<String, Set<String>> byClient = state.composites().get(composite.realmRole());
            if (byClient == null) {
                return true;
            }
            Set<String> actualCompositeClientRoles = byClient.get(composite.clientId());
            if (actualCompositeClientRoles == null || !actualCompositeClientRoles.containsAll(composite.clientRoles())) {
                return true;
            }
        }
        return false;
    }
}
