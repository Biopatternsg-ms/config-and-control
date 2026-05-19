package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineRepository pipelineRepository;

    public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
        var pipelineConfig = pipelineRepository.findById(id);
        if (pipelineConfig == null) {
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        pipelineConfig.setStep(step);
        pipelineConfig.addStatus(status);
        return pipelineRepository.save(pipelineConfig);
    }
}
