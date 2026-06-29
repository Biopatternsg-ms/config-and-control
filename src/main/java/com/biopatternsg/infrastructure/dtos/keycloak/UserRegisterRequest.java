package com.biopatternsg.infrastructure.dtos.keycloak;

import lombok.Builder;

import java.util.List;

@Builder
public record UserRegisterRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        List<String>realmRoles,
        List<UserCredentialsRequest> credentials,
        List<String> requiredActions
) {
}
