package com.biopatternsg.infrastructure.dtos.keycloak;

import jakarta.validation.constraints.Email;
import lombok.NonNull;

public record UserRequest(
        @NonNull @Email String username,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String password
) {
}
