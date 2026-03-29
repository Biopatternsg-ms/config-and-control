package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.FindPipeline;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class FindPipelineUseCase implements FindPipeline {

    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig byId(String id) {
        return pipelineRepository.findById(id);
    }

    @Override
    public PipelineConfig byName(String name) {
        return pipelineRepository.findByName(name);
    }

    @Override
    public List<PipelineConfig> byFilters(FindPipelineRequest findPipelineRequest) {
        return pipelineRepository.findByFilters(findPipelineRequest);
    }
}
