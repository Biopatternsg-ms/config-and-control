package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import com.biopatternsg.infrastructure.dtos.PipelineResponse;

import java.util.List;

public interface PipelineRepository {

    PipelineConfig save(PipelineConfig pipelineConfig);
    PipelineConfig findById(String id);
    PipelineConfig findByName(String name);
    PipelineConfig findByNameExists(String networkId, String name);
    List<PipelineResponse> findByFilters(FindPipelineRequest findPipelineRequest);
}
