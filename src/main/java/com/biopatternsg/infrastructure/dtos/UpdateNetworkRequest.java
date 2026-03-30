package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;

public record UpdateNetworkRequest(

        @NotNull String id,
        @NotNull String name,
        @NotNull String description
) {
}
