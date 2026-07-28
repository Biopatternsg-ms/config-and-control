package com.biopatternsg.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "El campo 'enabled' es obligatorio y no puede ser nulo")
        @JsonProperty("enabled")
        Boolean enabled
) {
}
