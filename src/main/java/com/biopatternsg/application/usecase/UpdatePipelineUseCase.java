package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineUseCase implements UpdatePipeline {

    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(PipelineConfig pipelineConfig) {

        var pipelineCurrentConfig = pipelineRepository.findById(pipelineConfig.getId());
        if(pipelineCurrentConfig == null){
            //TODO Agregar excepción
            return null;
        }

        //pipelineCurrentConfig.set
        return pipelineRepository.save(pipelineCurrentConfig);
    }
}
