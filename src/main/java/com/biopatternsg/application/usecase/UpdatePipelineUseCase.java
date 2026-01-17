package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
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

        var pipelineConfigCurrent = pipelineRepository.findByNameExists(pipelineConfig.getId(), pipelineConfig.getName());
        if(pipelineConfigCurrent != null){
            throw new UnprocessableEntityException("The pipeline name already exists");
        }

        pipelineConfigCurrent = pipelineRepository.findById(pipelineConfig.getId());
        if(pipelineConfigCurrent == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        pipelineConfigCurrent.setName(pipelineConfig.getName());
        pipelineConfigCurrent.setDescription(pipelineConfig.getDescription());
        pipelineConfigCurrent.setNetworkId(pipelineConfig.getNetworkId());
        pipelineConfigCurrent.setExpertObjects(pipelineConfig.getExpertObjects());
        pipelineConfigCurrent.setTranscriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig());
        pipelineConfigCurrent.setLevels(pipelineConfig.getLevels());

        return pipelineRepository.save(pipelineConfigCurrent);
    }
}
