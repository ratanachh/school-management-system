package com.visor.school.userservice.bootstrap

import com.visor.school.keycloak.InitializerService
import com.visor.school.keycloak.config.InitializerProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class KeycloakInitializerRunner(
    private val initializerService: InitializerService,
    private val properties: InitializerProperties
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (!properties.enabled) {
            log.info("Keycloak initializer disabled via configuration; skipping startup provisioning")
            return
        }

        val blueprint = properties.toBlueprint()
        val retry = properties.retry
        var attempt = 1
        var backoff = retry.initialBackoffMillis

        while (attempt <= retry.maxAttempts) {
            try {
                val outcome = initializerService.initialize(blueprint)
                log.info("Keycloak initializer outcome: {}", outcome)
                return
            } catch (ex: Exception) {
                if (attempt == retry.maxAttempts) {
                    if (retry.failOnError) {
                        log.error("Keycloak initialization failed after {} attempts", attempt, ex)
                        throw ex
                    } else {
                        log.warn(
                            "Keycloak initialization failed after {} attempts. Application will start without Keycloak initialization. Error: {}",
                            attempt,
                            ex.message
                        )
                        return
                    }
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
                    if (retry.failOnError) {
                        throw ex
                    } else {
                        log.warn("Keycloak initialization interrupted. Application will start without Keycloak initialization.")
                        return
                    }
                }

                backoff = (backoff * retry.multiplier).toLong()
                attempt++
            }
        }
    }
}
