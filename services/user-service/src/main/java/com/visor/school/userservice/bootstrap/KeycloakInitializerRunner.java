package com.visor.school.userservice.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.visor.school.keycloak.InitializerService;
import com.visor.school.keycloak.config.InitializerProperties;
import com.visor.school.keycloak.model.InitializationOutcome;
import com.visor.school.keycloak.model.KeycloakBlueprint;

@Component
public class KeycloakInitializerRunner implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InitializerService initializerService;
    private final InitializerProperties properties;

    public KeycloakInitializerRunner(InitializerService initializerService, InitializerProperties properties) {
        this.initializerService = initializerService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isEnabled()) {
            log.info("Keycloak initializer disabled via configuration; skipping startup provisioning");
            return;
        }

        KeycloakBlueprint blueprint = properties.toBlueprint();
        InitializerProperties.Retry retry = properties.getRetry();
        int attempt = 1;
        long backoff = retry.getInitialBackoffMillis();

        while (attempt <= retry.getMaxAttempts()) {
            try {
                InitializationOutcome outcome = initializerService.initialize(blueprint);
                log.info("Keycloak initializer outcome: {}", outcome);
                return;
            } catch (Exception ex) {
                if (attempt == retry.getMaxAttempts()) {
                    if (retry.isFailOnError()) {
                        log.error("Keycloak initialization failed after {} attempts", attempt, ex);
                        throw ex;
                    } else {
                        log.warn(
                            "Keycloak initialization failed after {} attempts. Application will start without Keycloak initialization. Error: {}",
                            attempt,
                            ex.getMessage()
                        );
                        return;
                    }
                }

                log.warn(
                    "Keycloak initialization attempt {} failed: {}. Retrying in {} ms",
                    attempt,
                    ex.getMessage(),
                    backoff
                );

                try {
                    Thread.sleep(Math.max(100L, backoff));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    if (retry.isFailOnError()) {
                        throw ex;
                    } else {
                        log.warn("Keycloak initialization interrupted. Application will start without Keycloak initialization.");
                        return;
                    }
                }

                backoff = (long) (backoff * retry.getMultiplier());
                attempt++;
            }
        }
    }
}
