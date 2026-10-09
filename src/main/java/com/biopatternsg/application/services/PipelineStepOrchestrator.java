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
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerInferences;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.services.PipelineService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PipelineStepOrchestrator {

    private final TriggerPubmedIntegration triggerPubmedIntegration;
    private final TriggerInferences triggerInferences;
    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineService pipelineService;

    public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
        switch (step) {
            case SEARCH_LEVELS -> {
                log.info("Step is SEARCH_LEVELS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeBuildsPairs(pipelineConfig);
            }
            case COMBINATIONS -> {
                log.info("Step is COMBINATIONS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeSearchPubmedIds(pipelineConfig);
            }
            case SEARCH_PUBMED_IDS -> {
                log.info("Step is SEARCH_PUBMED_IDS, triggering PubTator integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeSearchPubtator(pipelineConfig);
            }
            case SEARCH_PUBTATOR -> {
                log.info("Step is BUILD_KNOWLEDGE_BASE, triggering knowledge base build for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeBuildKnowledgeBase(pipelineConfig);
            }
            case BUILD_KNOWLEDGE_BASE -> {
                log.info("Step is BUILD_KNOWLEDGE_BASE completed, triggering expert objects alignment for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeGenerateAlignedObjects(pipelineConfig);
            }
            case GENERATE_ALIGNED_OBJECTS -> {
                log.info("Step is GENERATE_ALIGNED_OBJECTS completed, setting UPDATE_ALIGNED_OBJECTS to IN_PROGRESS for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.UPDATE_ALIGNED_OBJECTS, Status.IN_PROGRESS);
            }
            case UPDATE_ALIGNED_OBJECTS -> {
                log.info("Step UPDATE_ALIGNED_OBJECTS completed successfully for pipeline {}, setting CONFIGURE_INFERENCES to IN_PROGRESS", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.CONFIGURE_INFERENCES, Status.IN_PROGRESS);
            }
            case CONFIGURE_INFERENCES -> {
                log.info("Step CONFIGURE_INFERENCES completed successfully for pipeline {}, triggering FIND_ROLES", pipelineConfig.getId());
                triggerInferences.executeFindRoles(pipelineConfig);
            }
            case FIND_ROLES -> {
                log.info("Step FIND_ROLES completed successfully for pipeline {}, setting UPDATE_BIOLOGICAL_OBJECTS to IN_PROGRESS", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.UPDATE_BIOLOGICAL_OBJECTS, Status.IN_PROGRESS);
            }
            case UPDATE_BIOLOGICAL_OBJECTS -> {
                log.info("Step UPDATE_BIOLOGICAL_OBJECTS completed successfully for pipeline {}", pipelineConfig.getId());
            }
            default -> {
                log.info("Step is unknown, doing nothing");
            }
        }
    }
}
