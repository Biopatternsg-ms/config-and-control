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
package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineResumeDetail;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.models.ResumeFailedPipelinesResponse;
import com.biopatternsg.domain.port.in.ResumeFailedPipelines;
import com.biopatternsg.domain.port.out.UserSessionPort;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ResumeFailedPipelinesUseCase implements ResumeFailedPipelines {

    private final PipelineRepository pipelineRepository;
    private final NetworkRepository networkRepository;
    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineStepOrchestrator pipelineStepOrchestrator;
    private final UserSessionPort userSessionPort;

    @Override
    public ResumeFailedPipelinesResponse execute() {
        log.info("Starting execution of ResumeFailedPipelinesUseCase...");
        List<PipelineConfig> failedPipelines = pipelineRepository.findAllFailed();

        if (failedPipelines == null || failedPipelines.isEmpty()) {
            log.info("No failed pipelines found for resumption");
            return ResumeFailedPipelinesResponse.builder()
                    .totalFailed(0)
                    .resumed(0)
                    .skipped(0)
                    .errors(0)
                    .details(new ArrayList<>())
                    .build();
        }

        int totalFailed = failedPipelines.size();
        int resumed = 0;
        int skipped = 0;
        int errors = 0;
        List<PipelineResumeDetail> details = new ArrayList<>();

        for (PipelineConfig pipeline : failedPipelines) {
            PipelineSteps failedStep = determineFailedStep(pipeline);
            PipelineSteps predecessorStep = determinePredecessor(failedStep);

            // 1. Resolve owner userId from parent Network
            String userId = null;
            try {
                if (pipeline.getNetworkId() != null) {
                    NetworkConfig network = networkRepository.findById(pipeline.getNetworkId());
                    if (network != null) {
                        userId = network.getUserId();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not retrieve network for pipeline {}: {}", pipeline.getId(), e.getMessage());
            }

            // Establish tenant context in UserSessionPort if present
            if (userId != null && !userId.isBlank()) {
                userSessionPort.setUserId(userId);
            }

            // 2. Exclude manual review steps
            if (isManualStep(failedStep)) {
                log.info("Pipeline {} failed/paused at manual step {}, skipping auto-dispatch", pipeline.getId(), failedStep);
                skipped++;
                details.add(PipelineResumeDetail.builder()
                        .pipelineId(pipeline.getId())
                        .networkId(pipeline.getNetworkId())
                        .userId(userId)
                        .failedStep(failedStep)
                        .resumedFromStep(null)
                        .resultStatus("SKIPPED_MANUAL_STEP")
                        .message("Pipeline waiting for manual user validation in UI")
                        .build());
                continue;
            }

            // 3. Dispatch automated step with error isolation
            try {
                dispatchResumption(pipeline, failedStep, predecessorStep);
                resumed++;
                details.add(PipelineResumeDetail.builder()
                        .pipelineId(pipeline.getId())
                        .networkId(pipeline.getNetworkId())
                        .userId(userId)
                        .failedStep(failedStep)
                        .resumedFromStep(predecessorStep)
                        .resultStatus("RESUMED")
                        .message("Successfully re-dispatched to orchestrator")
                        .build());
                log.info("Successfully re-dispatched pipeline {} from predecessor {}", pipeline.getId(), predecessorStep);
            } catch (Exception e) {
                log.error("Error resuming pipeline {}: {}", pipeline.getId(), e.getMessage(), e);
                errors++;
                details.add(PipelineResumeDetail.builder()
                        .pipelineId(pipeline.getId())
                        .networkId(pipeline.getNetworkId())
                        .userId(userId)
                        .failedStep(failedStep)
                        .resumedFromStep(predecessorStep)
                        .resultStatus("ERROR")
                        .message(e.getMessage() != null ? e.getMessage() : "Unknown error during resumption")
                        .build());
            }
        }

        log.info("ResumeFailedPipelinesUseCase completed. Total: {}, Resumed: {}, Skipped: {}, Errors: {}",
                totalFailed, resumed, skipped, errors);

        return ResumeFailedPipelinesResponse.builder()
                .totalFailed(totalFailed)
                .resumed(resumed)
                .skipped(skipped)
                .errors(errors)
                .details(details)
                .build();
    }

    private void dispatchResumption(PipelineConfig pipeline, PipelineSteps failedStep, PipelineSteps predecessorStep) {
        if (failedStep == PipelineSteps.LAUNCH || failedStep == PipelineSteps.SEARCH_LEVELS || failedStep == PipelineSteps.EXPERT_OBJECTS || failedStep == PipelineSteps.TRANSCRIPTION_FACTOR) {
            log.info("Re-dispatching initial biological discovery for pipeline {}", pipeline.getId());
            biologicalObjectRepository.launch(pipeline);
        } else if (predecessorStep != null) {
            log.info("Re-dispatching pipeline {} via orchestrator with predecessor step {}", pipeline.getId(), predecessorStep);
            pipelineStepOrchestrator.orchestrate(pipeline, predecessorStep);
        }
    }

    private PipelineSteps determineFailedStep(PipelineConfig pipeline) {
        if (pipeline.getStatuses() == null || pipeline.getStatuses().isEmpty()) {
            return pipeline.getStep() != null ? pipeline.getStep() : PipelineSteps.LAUNCH;
        }
        for (int i = pipeline.getStatuses().size() - 1; i >= 0; i--) {
            PipelineStatus ps = pipeline.getStatuses().get(i);
            if (ps.getStatus() == Status.FAILED) {
                return ps.getStep();
            }
        }
        return pipeline.getStep() != null ? pipeline.getStep() : PipelineSteps.LAUNCH;
    }

    private boolean isManualStep(PipelineSteps step) {
        return step == PipelineSteps.UPDATE_ALIGNED_OBJECTS || step == PipelineSteps.CONFIGURE_INFERENCES;
    }

    private PipelineSteps determinePredecessor(PipelineSteps step) {
        if (step == null) {
            return null;
        }
        return switch (step) {
            case CONFIG, LAUNCH, TRANSCRIPTION_FACTOR, EXPERT_OBJECTS, SEARCH_LEVELS -> null;
            case COMBINATIONS -> PipelineSteps.SEARCH_LEVELS;
            case SEARCH_PUBMED_IDS -> PipelineSteps.COMBINATIONS;
            case SEARCH_PUBTATOR -> PipelineSteps.SEARCH_PUBMED_IDS;
            case BUILD_KNOWLEDGE_BASE -> PipelineSteps.SEARCH_PUBTATOR;
            case GENERATE_ALIGNED_OBJECTS -> PipelineSteps.BUILD_KNOWLEDGE_BASE;
            case UPDATE_ALIGNED_OBJECTS -> PipelineSteps.GENERATE_ALIGNED_OBJECTS;
            case CONFIGURE_INFERENCES -> PipelineSteps.UPDATE_ALIGNED_OBJECTS;
            case FIND_ROLES -> PipelineSteps.CONFIGURE_INFERENCES;
            case UPDATE_BIOLOGICAL_OBJECTS -> PipelineSteps.FIND_ROLES;
            default -> null;
        };
    }
}
