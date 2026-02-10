package com.visor.school.userservice.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    int expiresIn,
    int refreshExpiresIn,
    String tokenType
) {
    public LoginResponse(String accessToken, String refreshToken, int expiresIn, int refreshExpiresIn) {
        this(accessToken, refreshToken, expiresIn, refreshExpiresIn, "Bearer");
    }
}
