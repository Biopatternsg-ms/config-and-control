/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
