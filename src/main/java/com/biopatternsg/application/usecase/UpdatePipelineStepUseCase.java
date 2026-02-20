package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineStepUseCase implements UpdatePipelineStep {

    private final PipelineRepository pipelineRepository;

    @Override
    public void execute(String pipelineId, PipelineStepRequest stepRequest) {

        var pipelineConfig = pipelineRepository.findById(pipelineId);
        if(pipelineConfig == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        pipelineConfig.setStep(stepRequest.step());
        pipelineRepository.save(pipelineConfig);
        log.info("Pipeline {}, step {} updated",pipelineId, stepRequest.step().getValue());

    }
}
