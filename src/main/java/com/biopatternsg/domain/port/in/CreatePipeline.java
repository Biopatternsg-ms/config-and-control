package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.CreatePipelineRequest;

public interface CreatePipeline {

    PipelineConfig execute(CreatePipelineRequest createPipeline);
}
