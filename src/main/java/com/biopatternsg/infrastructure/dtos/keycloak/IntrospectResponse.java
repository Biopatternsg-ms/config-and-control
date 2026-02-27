package com.biopatternsg.infrastructure.dtos.keycloak;

public record IntrospectResponse(

        boolean active,
        String username,
        String sub,
        long exp
) {
}
