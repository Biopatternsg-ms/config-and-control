package com.biopatternsg.infrastructure.dtos;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;

import java.util.List;

public record LaunchPipelineRequest(

        String pipelineId,
        Integer levels,
        List<ExpertObjectConfig>expertObjects,
        TranscriptionFactorConfig transcriptionFactorConfig
) {
}
