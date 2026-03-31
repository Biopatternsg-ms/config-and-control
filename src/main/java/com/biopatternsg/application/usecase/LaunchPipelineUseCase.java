package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.port.in.LaunchPipeline;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.LaunchPipelineRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;


@ApplicationScoped
@RequiredArgsConstructor
public class LaunchPipelineUseCase implements LaunchPipeline {

    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineRepository pipelineRepository;

    @Override
    public void execute(LaunchPipelineRequest pipelineRequest) {

        var pipeline = pipelineRepository.findById(pipelineRequest.pipelineId());
        if(pipeline == null){
            throw new UnprocessableEntityException("The pipeline don't exist");
        }

        biologicalObjectRepository.launch(pipeline);

        pipeline.setStep(PipelineSteps.LAUNCH);
        pipelineRepository.save(pipeline);
    }
}
