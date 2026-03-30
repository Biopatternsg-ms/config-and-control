package com.biopatternsg.infrastructure.dtos.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(

        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("email")
        String email,
        @JsonProperty("firstName")
        String firstName,
        @JsonProperty("lastName")
        String lastName,
        @JsonProperty("enabled")
        String enabled,
        @JsonProperty("emailVerified")
        String emailVerified,
        @JsonProperty("createdTimestamp")
        Long createdAt
) {
}
