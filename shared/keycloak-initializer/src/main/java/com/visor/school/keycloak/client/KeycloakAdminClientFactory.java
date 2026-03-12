package com.visor.school.keycloak.client;

import java.util.function.Function;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.visor.school.keycloak.config.InitializerProperties;

import jakarta.ws.rs.client.Client;

/**
 * Factory that produces short-lived Keycloak admin clients for performing provisioning operations.
 * Each invocation creates a new client instance to avoid stale tokens and ensure thread-safety.
 */
@Component
public class KeycloakAdminClientFactory {

    private static final String ADMIN_REALM = "master";
    private final InitializerProperties properties;

    public KeycloakAdminClientFactory(InitializerProperties properties) {
        this.properties = properties;
    }

    public <T> T withAdminClient(Function<Keycloak, T> action) {
        InitializerProperties.Admin admin = properties.getAdmin();

        // Configure ObjectMapper to ignore unknown properties from newer Keycloak versions
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Client resteasyClient = new ResteasyClientBuilderImpl()
            .register(new JacksonJsonProvider(objectMapper))
            .build();

        try (Keycloak client = KeycloakBuilder.builder()
            .serverUrl(admin.getUrl())
            .realm(ADMIN_REALM)
            .clientId(admin.getClientId())
            .username(admin.getUsername())
            .password(admin.getPassword())
            .grantType(OAuth2Constants.PASSWORD)
            .resteasyClient(resteasyClient)
            .build()) {
            
            return action.apply(client);
        } finally {
            resteasyClient.close();
        }
    }
}
