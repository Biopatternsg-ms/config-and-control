package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePipelineRequest(

        @NotNull String name,
        @NotNull String description,
        @NotNull String networkId,
        @NotNull Integer levels,
        @NotNull List<ExpertObjectConfig> expertObjects,
        @NotNull TranscriptionFactorConfig transcriptionFactorConfig
) {
}
