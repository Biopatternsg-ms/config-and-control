package com.biopatternsg.infrastructure.dtos.keycloak;

public record AuthenticationResponse(
        String access_token,
        String refresh_token,
        String token_type,
        String id_token,
        long expires_in
) {
}
