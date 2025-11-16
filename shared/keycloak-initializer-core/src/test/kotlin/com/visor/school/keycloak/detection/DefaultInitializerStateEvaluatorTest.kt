package com.visor.school.keycloak.detection

import com.visor.school.keycloak.model.ClientRoleBlueprint
import com.visor.school.keycloak.model.KeycloakBlueprint
import com.visor.school.keycloak.model.RealmBlueprint
import com.visor.school.keycloak.model.RoleBlueprint
import com.visor.school.keycloak.model.RoleCompositeMapping
import com.visor.school.keycloak.state.KeycloakStateReader
import com.visor.school.keycloak.state.RealmState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultInitializerStateEvaluatorTest {

    private val blueprint = KeycloakBlueprint(
        realm = RealmBlueprint(name = "test-realm"),
        realmRoles = listOf(RoleBlueprint("SUPER_ADMIN")),
        clientRoles = listOf(ClientRoleBlueprint(clientId = "test-client", name = "MANAGE_ATTENDANCE")),
        composites = listOf(
            RoleCompositeMapping(
                realmRole = "SUPER_ADMIN",
                clientId = "test-client",
                clientRoles = listOf("MANAGE_ATTENDANCE")
            )
        )
    )

    @Test
    fun `realm missing triggers provisioning`() {
        val evaluator = DefaultInitializerStateEvaluator(stubStateReader(RealmState(exists = false, initializedFlag = false)))
        val decision = evaluator.evaluate(blueprint)

        assertEquals(true, decision.performProvisioning)
    }

    @Test
    fun `missing initialization flag triggers provisioning`() {
        val state = RealmState(
            exists = true,
            initializedFlag = false,
            realmRoles = setOf("SUPER_ADMIN"),
            clientRoles = mapOf("test-client" to setOf("MANAGE_ATTENDANCE")),
            composites = mapOf("SUPER_ADMIN" to mapOf("test-client" to setOf("MANAGE_ATTENDANCE")))
        )
        val evaluator = DefaultInitializerStateEvaluator(stubStateReader(state))

        val decision = evaluator.evaluate(blueprint)
        assertEquals(true, decision.performProvisioning)
    }

    @Test
    fun `missing client role triggers provisioning`() {
        val state = RealmState(
            exists = true,
            initializedFlag = true,
            realmRoles = setOf("SUPER_ADMIN"),
            clientRoles = emptyMap(),
            composites = emptyMap()
        )
        val evaluator = DefaultInitializerStateEvaluator(stubStateReader(state))

        val decision = evaluator.evaluate(blueprint)
        assertEquals(true, decision.performProvisioning)
    }

    @Test
    fun `complete state skips provisioning`() {
        val state = RealmState(
            exists = true,
            initializedFlag = true,
            realmRoles = setOf("SUPER_ADMIN"),
            clientRoles = mapOf("test-client" to setOf("MANAGE_ATTENDANCE")),
            composites = mapOf("SUPER_ADMIN" to mapOf("test-client" to setOf("MANAGE_ATTENDANCE")))
        )
        val evaluator = DefaultInitializerStateEvaluator(stubStateReader(state))

        val decision = evaluator.evaluate(blueprint)
        assertEquals(false, decision.performProvisioning)
    }

    private fun stubStateReader(state: RealmState): KeycloakStateReader = KeycloakStateReader { state }
}

