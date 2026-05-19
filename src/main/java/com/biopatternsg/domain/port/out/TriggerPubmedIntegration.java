package com.biopatternsg.domain.port.out;

import com.biopatternsg.domain.models.PipelineConfig;

public interface TriggerPubmedIntegration {
    void execute(PipelineConfig pipelineConfig);
}
