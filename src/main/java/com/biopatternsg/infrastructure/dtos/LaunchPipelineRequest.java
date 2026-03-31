package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.NotNull;

public record LaunchPipelineRequest(

        @NotNull String pipelineId
) {
}
