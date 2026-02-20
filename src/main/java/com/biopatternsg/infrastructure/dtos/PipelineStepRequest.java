package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.enums.PipelineSteps;

public record PipelineStepRequest(
        PipelineSteps step
) {
}
