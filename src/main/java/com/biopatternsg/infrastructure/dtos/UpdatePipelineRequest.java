package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePipelineRequest(

        @NotNull String id,
        @NotNull String name,
        @NotNull String description,
        @NotNull Integer levels,
        @NotNull List<ExpertObjectConfig> expertObjects,
        @NotNull TranscriptionFactorConfig transcriptionFactorConfig
) {
}
