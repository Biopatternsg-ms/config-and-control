package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;

public interface UpdatePipelineStep {

    void execute(PipelineStepRequest stepRequest);
}
