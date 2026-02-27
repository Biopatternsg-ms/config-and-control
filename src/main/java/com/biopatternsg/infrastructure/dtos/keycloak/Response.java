package com.biopatternsg.infrastructure.dtos.keycloak;

public record Response(

        String access_token,
        String refresh_token,
        String token_type,
        String id_token,
        long expires_in
) {
}
