package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.services.PipelineService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.infrastructure.clients.PubmedRestClient;
import com.biopatternsg.infrastructure.dtos.BuildPairsRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class PubmedIntegrationAdapter implements TriggerPubmedIntegration {

    @RestClient
    @Inject
    private final PubmedRestClient pubmedRestClient;
    private final PipelineService pipelineService;

    public PubmedIntegrationAdapter(@RestClient PubmedRestClient pubmedRestClient, PipelineService pipelineService) {
        this.pubmedRestClient = pubmedRestClient;
        this.pipelineService = pipelineService;
    }

    @Override
    public void execute(PipelineConfig pipelineConfig) {
        int levels = pipelineConfig.getLevels() != null ? pipelineConfig.getLevels() : 1;
        BuildPairsRequest request = new BuildPairsRequest(
                pipelineConfig.getId(),
                pipelineConfig.isUseOnlyPrincipalName(),
                levels
        );
        try {
            pubmedRestClient.buildPairs(request);
            pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.COMBINATIONS, Status.IN_PROGRESS);
            log.info("Pubmed API buildPairs called successfully for pipeline {}", pipelineConfig.getId());
        } catch (Exception e) {
            pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.COMBINATIONS, Status.FAILED);
            log.error("Error calling Pubmed API buildPairs for pipeline {}", pipelineConfig.getId(), e);
        }
    }
}
