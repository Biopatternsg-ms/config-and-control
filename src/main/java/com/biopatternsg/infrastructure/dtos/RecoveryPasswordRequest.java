package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RecoveryPasswordRequest {
    @NotNull String username;
}
