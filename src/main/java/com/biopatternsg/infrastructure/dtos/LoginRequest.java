package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull String username,
        @NotNull String password
) {
}
