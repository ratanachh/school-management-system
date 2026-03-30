package com.visor.school.keycloak.integration;

public class KeycloakException extends RuntimeException {
    public KeycloakException(String message) {
        super(message);
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }
}
