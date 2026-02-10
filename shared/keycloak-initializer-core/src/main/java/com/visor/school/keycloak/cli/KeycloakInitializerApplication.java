package com.visor.school.keycloak.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.visor.school.keycloak.InitializerService;
import com.visor.school.keycloak.config.InitializerProperties;
import com.visor.school.keycloak.model.InitializationOutcome;
import com.visor.school.keycloak.model.KeycloakBlueprint;

@SpringBootApplication(scanBasePackages = "com.visor.school.keycloak")
public class KeycloakInitializerApplication implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InitializerService initializerService;
    private final InitializerProperties properties;

    public KeycloakInitializerApplication(InitializerService initializerService, InitializerProperties properties) {
        this.initializerService = initializerService;
        this.properties = properties;
    }

    public static void main(String[] args) {
        SpringApplication.run(KeycloakInitializerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!properties.isEnabled()) {
            log.warn("Keycloak initializer is disabled; set keycloak.initializer.enabled=true to run manually");
            return;
        }

        KeycloakBlueprint blueprint = properties.toBlueprint();
        InitializerProperties.Retry retry = properties.getRetry();
        int attempt = 1;
        long backoff = retry.getInitialBackoffMillis();

        while (attempt <= retry.getMaxAttempts()) {
            try {
                InitializationOutcome outcome = initializerService.initialize(blueprint);
                log.info("Keycloak initialization finished with status {}", outcome.status());
                log.info(outcome.message());
                return;
            } catch (Exception ex) {
                if (attempt == retry.getMaxAttempts()) {
                    log.error("Keycloak initialization failed after {} attempts", attempt, ex);
                    throw ex;
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
                    throw ex;
                }

                backoff = (long) (backoff * retry.getMultiplier());
                attempt++;
            }
        }
    }
}
