package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import jakarta.validation.constraints.NotNull;

public record PipelineStepRequest(
       @NotNull String id,
       @NotNull PipelineSteps step,
       @NotNull Status status
) {
}
