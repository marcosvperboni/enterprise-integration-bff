package com.marcosperboni.integrationbff.web.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {

    public static TokenResponse of(String accessToken, long expiresInSeconds) {
        return new TokenResponse(accessToken, "Bearer", expiresInSeconds);
    }
}
