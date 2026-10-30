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
import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineResumeSummary;
import com.biopatternsg.domain.port.in.ResumePipeline;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ResumePipelineUseCase implements ResumePipeline {

    private final PipelineRepository pipelineRepository;
    private final NetworkRepository networkRepository;
    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineStepOrchestrator pipelineStepOrchestrator;

    private static final List<PipelineSteps> BIOLOGICAL_STEPS = List.of(
            PipelineSteps.LAUNCH,
            PipelineSteps.TRANSCRIPTION_FACTOR,
            PipelineSteps.EXPERT_OBJECTS,
            PipelineSteps.SEARCH_LEVELS
    );

    private static final List<PipelineSteps> PUBMED_STEPS = List.of(
            PipelineSteps.COMBINATIONS,
            PipelineSteps.SEARCH_PUBMED_IDS,
            PipelineSteps.SEARCH_PUBTATOR,
            PipelineSteps.BUILD_KNOWLEDGE_BASE,
            PipelineSteps.GENERATE_ALIGNED_OBJECTS
    );

    @Override
    public PipelineResumeSummary execute(MicroService microService) {
        log.info("Starting pipeline resumption for microService: {}", microService);

        List<PipelineSteps> candidateSteps = microService == MicroService.SEARCH_BIOLOGICAL_OBJECTS
                ? BIOLOGICAL_STEPS
                : PUBMED_STEPS;

        List<PipelineConfig> candidates = pipelineRepository.findInProgressBySteps(candidateSteps);
        if (candidates == null) {
            candidates = List.of();
        }

        log.info("Found {} candidate pipelines in progress for microService {}", candidates.size(), microService);

        List<String> resumedPipelineIds = new ArrayList<>();
        List<String> failedPipelineIds = new ArrayList<>();
        Map<String, String> errors = new HashMap<>();

        for (PipelineConfig pipeline : candidates) {
            processPipeline(pipeline, microService, resumedPipelineIds, failedPipelineIds, errors);
        }

        PipelineResumeSummary summary = PipelineResumeSummary.builder()
                .microService(microService)
                .totalFound(candidates.size())
                .resumedCount(resumedPipelineIds.size())
                .failedCount(failedPipelineIds.size())
                .resumedPipelineIds(resumedPipelineIds)
                .failedPipelineIds(failedPipelineIds)
                .errors(errors)
                .build();

        log.info("Resumption batch completed for {}: total={}, resumed={}, failed={}",
                microService, summary.getTotalFound(), summary.getResumedCount(), summary.getFailedCount());

        return summary;
    }

    private void processPipeline(PipelineConfig pipeline,
                                 MicroService microService,
                                 List<String> resumedPipelineIds,
                                 List<String> failedPipelineIds,
                                 Map<String, String> errors) {
        String pipelineId = pipeline.getId();
        try {
            String userId = resolveUserId(pipeline);
            if (userId == null || userId.isBlank()) {
                failedPipelineIds.add(pipelineId);
                errors.put(pipelineId, "Cannot resolve userId for network: " + pipeline.getNetworkId());
                log.warn("Cannot resume pipeline {}: userId is missing or empty for network {}",
                        pipelineId, pipeline.getNetworkId());
                return;
            }

            if (microService == MicroService.SEARCH_BIOLOGICAL_OBJECTS) {
                resumeBiologicalPipeline(pipeline, userId);
            } else {
                resumePubmedPipeline(pipeline);
            }

            resumedPipelineIds.add(pipelineId);
            log.info("Successfully resumed pipeline {} for {}", pipelineId, microService);

        } catch (Exception e) {
            failedPipelineIds.add(pipelineId);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            errors.put(pipelineId, errorMsg);
            log.error("Failed to resume pipeline {}: {}", pipelineId, errorMsg, e);
        }
    }

    private String resolveUserId(PipelineConfig pipeline) {
        if (pipeline.getNetworkId() == null || pipeline.getNetworkId().isBlank()) {
            return null;
        }
        var network = networkRepository.findById(pipeline.getNetworkId());
        return network != null ? network.getUserId() : null;
    }

    private void resumeBiologicalPipeline(PipelineConfig pipeline, String userId) {
        biologicalObjectRepository.launchConsole(pipeline, userId);
    }

    private void resumePubmedPipeline(PipelineConfig pipeline) {
        PipelineSteps previousStep = resolvePreviousStep(pipeline.getStep());
        if (previousStep == null) {
            throw new IllegalStateException("Cannot determine predecessor step for step: " + pipeline.getStep());
        }
        pipelineStepOrchestrator.orchestrate(pipeline, previousStep);
    }

    private PipelineSteps resolvePreviousStep(PipelineSteps currentStep) {
        if (currentStep == null) {
            return null;
        }
        return switch (currentStep) {
            case COMBINATIONS -> PipelineSteps.SEARCH_LEVELS;
            case SEARCH_PUBMED_IDS -> PipelineSteps.COMBINATIONS;
            case SEARCH_PUBTATOR -> PipelineSteps.SEARCH_PUBMED_IDS;
            case BUILD_KNOWLEDGE_BASE -> PipelineSteps.SEARCH_PUBTATOR;
            case GENERATE_ALIGNED_OBJECTS -> PipelineSteps.BUILD_KNOWLEDGE_BASE;
            default -> null;
        };
    }
}
