package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.UpdatePipelineRequest;

public interface UpdatePipeline {

    PipelineConfig execute(UpdatePipelineRequest pipelineRequest);
}
