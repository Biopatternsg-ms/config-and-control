package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.enums.PipelineSteps;

public interface UpdatePipelineStep {

    void execute(String pipelineId, PipelineSteps step);
}
