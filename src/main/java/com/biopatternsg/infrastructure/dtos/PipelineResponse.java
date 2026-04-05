package com.biopatternsg.infrastructure.dtos;

import lombok.Builder;

@Builder
public record PipelineResponse(

        String id,
        String name,
        String description,
        Integer levels,
        int createdAt
) {
}
