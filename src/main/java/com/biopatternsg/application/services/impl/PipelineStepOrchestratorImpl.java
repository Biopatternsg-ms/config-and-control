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
package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.PipelineService;
import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerInferences;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PipelineStepOrchestratorImpl implements PipelineStepOrchestrator {

    private final TriggerPubmedIntegration triggerPubmedIntegration;
    private final TriggerInferences triggerInferences;
    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineService pipelineService;

    @Override
    public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
        switch (step) {
            case SEARCH_LEVELS -> {
                log.info("Step is SEARCH_LEVELS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.COMBINATIONS, Status.IN_PROGRESS);
                try {
                    triggerPubmedIntegration.executeBuildsPairs(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to execute builds pairs for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.COMBINATIONS, Status.FAILED);
                }
            }
            case COMBINATIONS -> {
                log.info("Step is COMBINATIONS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SEARCH_PUBMED_IDS, Status.IN_PROGRESS);
                try {
                    triggerPubmedIntegration.executeSearchPubmedIds(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to search pubmed IDs for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SEARCH_PUBMED_IDS, Status.FAILED);
                }
            }
            case SEARCH_PUBMED_IDS -> {
                log.info("Step is SEARCH_PUBMED_IDS, triggering PubTator integration for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SEARCH_PUBTATOR, Status.IN_PROGRESS);
                try {
                    triggerPubmedIntegration.executeSearchPubtator(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to search pubtator for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SEARCH_PUBTATOR, Status.FAILED);
                }
            }
            case SEARCH_PUBTATOR -> {
                log.info("Step is BUILD_KNOWLEDGE_BASE, triggering knowledge base build for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.BUILD_KNOWLEDGE_BASE, Status.IN_PROGRESS);
                try {
                    triggerPubmedIntegration.executeBuildKnowledgeBase(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to build knowledge base for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.BUILD_KNOWLEDGE_BASE, Status.FAILED);
                }
            }
            case BUILD_KNOWLEDGE_BASE -> {
                log.info("Step is BUILD_KNOWLEDGE_BASE completed, triggering expert objects alignment for pipeline {}", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.GENERATE_ALIGNED_OBJECTS, Status.IN_PROGRESS);
                try {
                    triggerPubmedIntegration.executeGenerateAlignedObjects(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to generate aligned objects for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.GENERATE_ALIGNED_OBJECTS, Status.FAILED);
                }
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
                log.info("Step CONFIGURE_INFERENCES completed successfully for pipeline {}, resetting downstream steps and triggering FIND_ROLES", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.UPDATE_BIOLOGICAL_OBJECTS, Status.PENDING);
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SELECT_CLOSING_OBJECTS, Status.PENDING);
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.INFER_PATHWAYS, Status.PENDING);
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.FIND_ROLES, Status.IN_PROGRESS);
                try {
                    triggerInferences.executeFindRoles(pipelineConfig);
                } catch (Exception e) {
                    log.error("Failed to execute find roles for pipeline {}", pipelineConfig.getId(), e);
                    pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.FIND_ROLES, Status.FAILED);
                }
            }
            case FIND_ROLES -> {
                log.info("Step FIND_ROLES completed successfully for pipeline {}, setting UPDATE_BIOLOGICAL_OBJECTS to IN_PROGRESS", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.UPDATE_BIOLOGICAL_OBJECTS, Status.IN_PROGRESS);
            }
            case UPDATE_BIOLOGICAL_OBJECTS -> {
                log.info("Step UPDATE_BIOLOGICAL_OBJECTS completed successfully for pipeline {}, setting SELECT_CLOSING_OBJECTS to IN_PROGRESS", pipelineConfig.getId());
                pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.SELECT_CLOSING_OBJECTS, Status.IN_PROGRESS);
            }
            case SELECT_CLOSING_OBJECTS -> {
                log.info("Step SELECT_CLOSING_OBJECTS completed successfully for pipeline {}", pipelineConfig.getId());
            }
            case INFER_PATHWAYS -> {
                log.info("Step INFER_PATHWAYS completed successfully for pipeline {}", pipelineConfig.getId());
            }
            default -> {
                log.info("Step is unknown, doing nothing");
            }
        }
    }
}
