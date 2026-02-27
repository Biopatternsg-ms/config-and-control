package com.biopatternsg.infrastructure.dtos.keycloak;

import java.util.List;

public record UserRegistration(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        List<String> realmRoles,
        List<UserCredentials> credentials
) {
}
