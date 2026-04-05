package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.enums.PipelineSteps;
import lombok.Builder;

@Builder
public record PipelineResponse(

        String id,
        String name,
        String description,
        PipelineSteps step,
        int createdAt
) {
}
