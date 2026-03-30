package com.visor.school.keycloak.integration;

public record KeycloakLoginResponse(
    String accessToken,
    String refreshToken,
    int expiresIn,
    int refreshExpiresIn,
    String tokenType
) {
    public KeycloakLoginResponse(String accessToken, String refreshToken, int expiresIn, int refreshExpiresIn) {
        this(accessToken, refreshToken, expiresIn, refreshExpiresIn, "Bearer");
    }
}
