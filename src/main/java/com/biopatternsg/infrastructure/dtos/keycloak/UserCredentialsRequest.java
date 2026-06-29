package com.biopatternsg.infrastructure.dtos.keycloak;

import lombok.Builder;

@Builder
public record UserCredentialsRequest(
        String type,
        String value,
        boolean temporary
) {
}
