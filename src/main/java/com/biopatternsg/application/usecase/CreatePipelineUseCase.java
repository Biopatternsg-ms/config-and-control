package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import com.biopatternsg.domain.port.in.CreatePipeline;

@ApplicationScoped
@RequiredArgsConstructor
public class CreatePipelineUseCase implements CreatePipeline {

    private final NetworkRepository networkRepository;
    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(PipelineConfig pipelineConfig) {

        //Network don't exists
        var networkConfig = networkRepository.findById(pipelineConfig.getNetworkId());
        if(networkConfig == null){
            throw new UnprocessableEntityException("The network don't exists");
        }

        //Build pipelineConfig
        var pipelineModel = pipelineRepository.findByName(networkConfig.getName());
        if(pipelineModel != null){
            throw new UnprocessableEntityException("The pipeline already exists");
        }

        return pipelineRepository.save(pipelineConfig);
    }
}
