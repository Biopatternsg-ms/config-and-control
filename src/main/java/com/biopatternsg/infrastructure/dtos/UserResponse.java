package com.biopatternsg.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UserResponse(

        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstName")
        String firstName,
        @JsonProperty("lastName")
        String lastName,
        @JsonProperty("enabled")
        boolean enabled,
        @JsonProperty("createdTimestamp")
        int createdAt
) {
}
