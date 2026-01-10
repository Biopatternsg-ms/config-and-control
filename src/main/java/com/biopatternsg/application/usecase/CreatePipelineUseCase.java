package com.biopatternsg.application.usecase;

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
            //TODO Agregar excepción
            return null;
        }

        //Build pipelineConfig
        var pipelineModel = pipelineRepository.findByName(networkConfig.getName());
        if(pipelineModel != null){
            //TODO agregar excepción
            return null;
        }

        return pipelineRepository.save(pipelineConfig);
    }
}
