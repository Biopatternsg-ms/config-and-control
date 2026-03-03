package com.biopatternsg.infrastructure.dtos.keycloak;

public record UserRequest(
        String email,
        String firstName,
        String lastName,
        String password
) {
}
