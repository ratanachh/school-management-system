package com.visor.school.userservice.bootstrap

import com.visor.school.keycloak.InitializerService
import com.visor.school.keycloak.KeycloakRealmProvisioner
import com.visor.school.keycloak.client.KeycloakAdminClientFactory
import com.visor.school.keycloak.config.InitializerProperties
import com.visor.school.keycloak.config.InitializerProperties.Client
import com.visor.school.keycloak.config.InitializerProperties.ClientRole
import com.visor.school.keycloak.config.InitializerProperties.Composite
import com.visor.school.keycloak.config.InitializerProperties.Realm
import com.visor.school.keycloak.config.InitializerProperties.Role
import com.visor.school.keycloak.detection.DefaultInitializerStateEvaluator
import com.visor.school.keycloak.model.InitializationStatus
import com.visor.school.keycloak.state.KeycloakAdminStateReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.junit.jupiter.api.Disabled

/**
 * Integration test for Keycloak initializer using Testcontainers
 * 
 * Note: This test requires Docker to be running
 * TODO: Add proper wait conditions for Keycloak container startup
 */
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
@Disabled("Requires Docker and proper Keycloak container wait conditions")
class KeycloakInitializerIntegrationTest {

    @Container
    private val keycloak: TestKeycloakContainer = TestKeycloakContainer(DockerImageName.parse(KEYCLOAK_IMAGE))
        .withAdminUsername("admin")
        .withAdminPassword("admin")

    @Test
    fun `initializer applies blueprint then skips when rerun`() {
        val properties = buildTestProperties()
        val factory = KeycloakAdminClientFactory(properties)
        val stateReader = KeycloakAdminStateReader(factory)
        val provisioner = KeycloakRealmProvisioner(factory)
        val initializer = InitializerService(DefaultInitializerStateEvaluator(stateReader), provisioner)
        val blueprint = properties.toBlueprint()

        val firstRun = initializer.initialize(blueprint)
        assertEquals(InitializationStatus.APPLIED, firstRun.status, "Expected first run to apply blueprint")

        val secondRun = initializer.initialize(blueprint)
        assertEquals(InitializationStatus.SKIPPED, secondRun.status, "Expected second run to skip provisioning")
    }

    private fun buildTestProperties(): InitializerProperties {
        val adminUrl = keycloak.authServerUrl
            .removeSuffix("/")
            .removeSuffix("/realms/master")
        return InitializerProperties(
            enabled = true,
            admin = InitializerProperties.Admin(
                url = adminUrl,
                username = "admin",
                password = "admin",
                clientId = "admin-cli"
            ),
            realm = Realm(
                name = TEST_REALM,
                enabled = true,
                attributes = mapOf("sso.system.initialized" to "true")
            ),
            clients = listOf(
                Client(
                    clientId = TEST_CLIENT_ID,
                    redirectUris = listOf("http://localhost:8080/*"),
                    webOrigins = listOf("+"),
                    attributes = mapOf("client.session.max.lifespan" to "36000")
                )
            ),
            realmRoles = listOf(
                Role(name = "SUPER_ADMIN", description = "Super administrator with full access"),
                Role(name = "TEACHER", description = "Teacher with attendance permissions")
            ),
            clientRoles = listOf(
                ClientRole(
                    clientId = TEST_CLIENT_ID,
                    name = "MANAGE_ATTENDANCE",
                    description = "Permission to manage attendance records"
                ),
                ClientRole(
                    clientId = TEST_CLIENT_ID,
                    name = "COLLECT_ATTENDANCE",
                    description = "Permission to collect attendance as class leader"
                )
            ),
            composites = listOf(
                Composite(
                    realmRole = "SUPER_ADMIN",
                    clientId = TEST_CLIENT_ID,
                    clientRoles = listOf("MANAGE_ATTENDANCE", "COLLECT_ATTENDANCE")
                ),
                Composite(
                    realmRole = "TEACHER",
                    clientId = TEST_CLIENT_ID,
                    clientRoles = listOf("COLLECT_ATTENDANCE")
                )
            )
        )
    }

    companion object {
        private const val KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:24.0.0"
        private const val TEST_REALM = "initializer-integration"
        private const val TEST_CLIENT_ID = "initializer-test-client"
    }
}

private class TestKeycloakContainer(imageName: DockerImageName) :
    GenericContainer<TestKeycloakContainer>(imageName) {

    init {
        withEnv("KEYCLOAK_ADMIN", "admin")
        withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
        withEnv("KC_HEALTH_ENABLED", "true")
        withEnv("KC_METRICS_ENABLED", "true")
        withExposedPorts(8080)
        withCommand("start-dev", "--http-port=8080")
    }

    fun withAdminUsername(username: String): TestKeycloakContainer = apply {
        withEnv("KEYCLOAK_ADMIN", username)
    }

    fun withAdminPassword(password: String): TestKeycloakContainer = apply {
        withEnv("KEYCLOAK_ADMIN_PASSWORD", password)
    }

    val authServerUrl: String
        get() = "http://${host}:${getMappedPort(8080)}"
}

