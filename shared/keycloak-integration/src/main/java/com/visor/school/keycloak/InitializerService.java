package com.visor.school.keycloak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.visor.school.keycloak.detection.InitializerStateEvaluator;
import com.visor.school.keycloak.model.InitializationDecision;
import com.visor.school.keycloak.model.InitializationOutcome;
import com.visor.school.keycloak.model.KeycloakBlueprint;

@Service
public class InitializerService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InitializerStateEvaluator stateEvaluator;
    private final RealmProvisioner realmProvisioner;

    public InitializerService(InitializerStateEvaluator stateEvaluator, RealmProvisioner realmProvisioner) {
        this.stateEvaluator = stateEvaluator;
        this.realmProvisioner = realmProvisioner;
    }

    public InitializationOutcome initialize(KeycloakBlueprint blueprint) {
        InitializationDecision decision = stateEvaluator.evaluate(blueprint);
        if (!decision.performProvisioning()) {
            String reason = decision.reason() != null ? decision.reason() : "Keycloak already initialized";
            log.info("Keycloak initializer skipped: {}", reason);
            return InitializationOutcome.skipped(reason);
        }

        log.info("Applying Keycloak initializer blueprint for realm '{}'", blueprint.realm().name());
        return realmProvisioner.applyBlueprint(blueprint);
    }
}
