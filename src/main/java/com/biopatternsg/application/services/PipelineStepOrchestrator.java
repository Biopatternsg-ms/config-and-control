package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PipelineStepOrchestrator {

    private final TriggerPubmedIntegration triggerPubmedIntegration;

    public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
        if (step == PipelineSteps.SEARCH_LEVELS) {
            log.info("Step is SEARCH_LEVELS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
            triggerPubmedIntegration.execute(pipelineConfig);
        }
    }
}
