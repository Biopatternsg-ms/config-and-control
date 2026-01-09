package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.FindPipeline;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
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
    public List<PipelineConfig> byNetwork(String networkId) {
        return pipelineRepository.findByNetworkId(networkId);
    }
}
