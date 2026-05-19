package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.domain.services.PipelineService;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineStepUseCase implements UpdatePipelineStep {

    private final PipelineService pipelineService;
    private final PipelineStepOrchestrator pipelineStepOrchestrator;

    @Override
    public void execute(PipelineStepRequest stepRequest) {

        var pipelineConfig = pipelineService.updateStep(
                stepRequest.id(),
                stepRequest.step(),
                stepRequest.status()
        );

        if (stepRequest.status() == Status.COMPLETED) {
            pipelineStepOrchestrator.orchestrate(pipelineConfig, stepRequest.step());
        }

        log.info("Pipeline {}, step {} updated",stepRequest.id(), stepRequest.step().getValue());
    }
}
