package com.visor.school.keycloak.cli

import com.visor.school.keycloak.InitializerService
import com.visor.school.keycloak.config.InitializerProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.math.max

@SpringBootApplication(scanBasePackages = ["com.visor.school.keycloak"])
class KeycloakInitializerApplication(
    private val initializerService: InitializerService,
    private val properties: InitializerProperties
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (!properties.enabled) {
            log.warn("Keycloak initializer is disabled; set keycloak.initializer.enabled=true to run manually")
            return
        }

        val blueprint = properties.toBlueprint()
        val retry = properties.retry
        var attempt = 1
        var backoff = retry.initialBackoffMillis

        while (attempt <= retry.maxAttempts) {
            try {
                val outcome = initializerService.initialize(blueprint)
                log.info("Keycloak initialization finished with status {}", outcome.status)
                log.info(outcome.message)
                return
            } catch (ex: Exception) {
                if (attempt == retry.maxAttempts) {
                    log.error("Keycloak initialization failed after {} attempts", attempt, ex)
                    throw ex
                }
                log.warn(
                    "Keycloak initialization attempt {} failed: {}. Retrying in {} ms",
                    attempt,
                    ex.message,
                    backoff
                )

                try {
                    Thread.sleep(max(100L, backoff))
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw ex
                }

                backoff = (backoff * retry.multiplier).toLong()
                attempt++
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KeycloakInitializerApplication>(*args)
}
