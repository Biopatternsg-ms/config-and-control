package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;

import java.util.List;

public interface PipelineRepository {

    PipelineConfig save(PipelineConfig pipelineConfig);
    PipelineConfig findById(String id);
    PipelineConfig findByName(String name);
    PipelineConfig findByNameExists(String id, String name);
    List<PipelineConfig> findByUserId();
    List<PipelineConfig> findByNetworkId(String networkId);
}
