package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;

import java.util.List;

public interface FindPipeline {

    PipelineConfig byId(String id);
    PipelineConfig byName(String name);
    List<PipelineConfig> byFilters(FindPipelineRequest findPipelineRequest);
}
