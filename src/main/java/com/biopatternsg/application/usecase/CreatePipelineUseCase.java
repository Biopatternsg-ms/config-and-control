package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.CreatePipelineRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import com.biopatternsg.domain.port.in.CreatePipeline;

@ApplicationScoped
@RequiredArgsConstructor
public class CreatePipelineUseCase implements CreatePipeline {

    private final NetworkRepository networkRepository;
    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(CreatePipelineRequest newPipeline) {

        var pipelineConfig = requestToConfig(newPipeline);
        //Network don't exists
        var networkConfig = networkRepository.findById(pipelineConfig.getNetworkId());
        if (networkConfig == null) {
            throw new UnprocessableEntityException("The network don't exists");
        }
        //Pipeline exists in network
        var findPipelineName = pipelineRepository.findByNameExists(pipelineConfig.getNetworkId(), pipelineConfig.getName());
        if(findPipelineName != null){
            throw new UnprocessableEntityException("The pipeline name already exists");
        }

        //Build pipelineConfig
        pipelineConfig.setStep(PipelineSteps.CONFIG);
        return pipelineRepository.save(pipelineConfig);
    }

    private PipelineConfig requestToConfig(CreatePipelineRequest newPipeline){

        return PipelineConfig.builder()
                .name(newPipeline.name())
                .description(newPipeline.description())
                .networkId(newPipeline.networkId())
                .levels(newPipeline.levels())
                .expertObjects(newPipeline.expertObjects())
                .transcriptionFactorConfig(newPipeline.transcriptionFactorConfig())
                .build();
    }
}
