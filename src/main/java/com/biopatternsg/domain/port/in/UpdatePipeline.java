package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;

public interface UpdatePipeline {

    PipelineConfig execute(PipelineConfig pipelineConfig);
}
