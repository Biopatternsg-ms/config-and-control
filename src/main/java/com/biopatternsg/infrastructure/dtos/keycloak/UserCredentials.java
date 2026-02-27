package com.biopatternsg.infrastructure.dtos.keycloak;

public record UserCredentials(
        String type,
        String value,
        boolean temporary
) {
}
