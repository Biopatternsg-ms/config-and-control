package com.biopatternsg.infrastructure.dtos;

public record BuildPairsRequest(
        String pipelineId,
        boolean useOnlyPrincipalName,
        int levels
) {
}
