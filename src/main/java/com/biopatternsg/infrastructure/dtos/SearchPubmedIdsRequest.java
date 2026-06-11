package com.biopatternsg.infrastructure.dtos;

public record SearchPubmedIdsRequest(
        String pipelineId,
        int retMax
) {
}
