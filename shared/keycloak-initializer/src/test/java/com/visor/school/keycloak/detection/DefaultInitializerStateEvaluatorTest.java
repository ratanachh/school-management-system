package com.visor.school.keycloak.detection;

import com.visor.school.keycloak.model.*;
import com.visor.school.keycloak.state.KeycloakStateReader;
import com.visor.school.keycloak.state.RealmState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultInitializerStateEvaluatorTest {

    private final KeycloakBlueprint blueprint = new KeycloakBlueprint(
        new RealmBlueprint("test-realm", true, Collections.emptyMap()),
        List.of(new ClientBlueprint("test-client", "openid-connect", false, true, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), null)),
        List.of(new RoleBlueprint("SUPER_ADMIN", null)),
        List.of(new ClientRoleBlueprint("test-client", "MANAGE_ATTENDANCE", null)),
        List.of(new RoleCompositeMapping("SUPER_ADMIN", "test-client", List.of("MANAGE_ATTENDANCE")))
    );

    @Test
    void realm_missing_triggers_provisioning() {
        DefaultInitializerStateEvaluator evaluator = new DefaultInitializerStateEvaluator(stubStateReader(new RealmState(false, false, Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap())));
        InitializationDecision decision = evaluator.evaluate(blueprint);

        Assertions.assertTrue(decision.performProvisioning());
    }

    @Test
    void missing_initialization_flag_triggers_provisioning() {
        RealmState state = new RealmState(
            true,
            false,
            Set.of("SUPER_ADMIN"),
            Map.of("test-client", Set.of("MANAGE_ATTENDANCE")),
            Map.of("SUPER_ADMIN", Map.of("test-client", Set.of("MANAGE_ATTENDANCE")))
        );
        DefaultInitializerStateEvaluator evaluator = new DefaultInitializerStateEvaluator(stubStateReader(state));

        InitializationDecision decision = evaluator.evaluate(blueprint);
        Assertions.assertTrue(decision.performProvisioning());
    }

    /*
    @Test
    void missing_client_role_triggers_provisioning() {
        RealmState state = new RealmState(
            true,
            true,
            Set.of("SUPER_ADMIN"),
            Collections.emptyMap(),
            Collections.emptyMap()
        );
        DefaultInitializerStateEvaluator evaluator = new DefaultInitializerStateEvaluator(stubStateReader(state));

        InitializationDecision decision = evaluator.evaluate(blueprint);
        Assertions.assertTrue(decision.performProvisioning());
    }
    */

    @Test
    void complete_state_skips_provisioning() {
        RealmState state = new RealmState(
            true,
            true,
            Set.of("SUPER_ADMIN"),
            Map.of("test-client", Set.of("MANAGE_ATTENDANCE")),
            Map.of("SUPER_ADMIN", Map.of("test-client", Set.of("MANAGE_ATTENDANCE")))
        );
        DefaultInitializerStateEvaluator evaluator = new DefaultInitializerStateEvaluator(stubStateReader(state));

        InitializationDecision decision = evaluator.evaluate(blueprint);
        Assertions.assertFalse(decision.performProvisioning());
    }

    private KeycloakStateReader stubStateReader(RealmState state) {
        return realmName -> state;
    }
}
