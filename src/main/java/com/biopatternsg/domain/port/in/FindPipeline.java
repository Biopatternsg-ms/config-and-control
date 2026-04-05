package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import com.biopatternsg.infrastructure.dtos.PipelineResponse;

import java.util.List;

public interface FindPipeline {

    PipelineConfig byId(String id);
    PipelineConfig byName(String name);
    List<PipelineResponse> byFilters(FindPipelineRequest findPipelineRequest);
}
