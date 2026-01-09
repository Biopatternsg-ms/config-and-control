package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;

public interface CreatePipeline {

    PipelineConfig execute(PipelineConfig pipelineConfig);
}
