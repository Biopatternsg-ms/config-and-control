package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;

public record CreateNetworkRequest(

        @NotNull String name,
        @NotNull String description
) {
}
