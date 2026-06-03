package com.biopatternsg.infrastructure.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {

    @NotNull
    @JsonProperty("refresh_token")
    String refreshToken;
}
