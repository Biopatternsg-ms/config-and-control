package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.enums.PipelineSteps;
import jakarta.validation.constraints.NotNull;

public record PipelineStepRequest(
       @NotNull String id,
       @NotNull PipelineSteps step
) {
}
