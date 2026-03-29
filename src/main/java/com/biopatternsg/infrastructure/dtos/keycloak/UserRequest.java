package com.biopatternsg.infrastructure.dtos.keycloak;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotNull @Email String username,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String password
) {
}
