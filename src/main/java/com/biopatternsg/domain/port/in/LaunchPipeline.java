package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.LaunchPipelineRequest;

public interface LaunchPipeline {
    void execute(LaunchPipelineRequest pipelineRequest);
}
