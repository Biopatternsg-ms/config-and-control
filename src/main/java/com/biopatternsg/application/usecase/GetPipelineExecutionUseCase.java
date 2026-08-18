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

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.port.in.GetPipelineExecution;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.domain.models.ExperimentExecutionResponse;
import com.biopatternsg.domain.models.PipelineStepExecutionResponse;
import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class GetPipelineExecutionUseCase implements GetPipelineExecution {

    private final PipelineRepository pipelineRepository;

    @Override
    public ExperimentExecutionResponse execute(String pipelineId) {
        PipelineConfig pipelineConfig = pipelineRepository.findById(pipelineId);
        if (pipelineConfig == null) {
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        List<PipelineStatus> statuses = pipelineConfig.getStatuses() != null
                ? pipelineConfig.getStatuses()
                : Collections.emptyList();

        List<PipelineStepExecutionResponse> stepResponses = new ArrayList<>();

        Date overallStartTime = null;
        Date overallEndTime = null;
        Date currentPhaseStartTime = null;

        for (PipelineSteps stepEnum : PipelineSteps.values()) {
            List<PipelineStatus> stepStatuses = statuses.stream()
                    .filter(ps -> ps.getStep() == stepEnum)
                    .toList();

            Optional<PipelineStatus> latestStatusOpt = stepStatuses.stream()
                    .filter(Objects::nonNull)
                    .filter(ps -> ps.getCreatedAt() != null)
                    .max(Comparator.comparing(PipelineStatus::getCreatedAt));

            Optional<PipelineStatus> completedStatus = stepStatuses.stream()
                    .filter(ps -> ps.getStatus() == Status.COMPLETED)
                    .findFirst();

            Optional<PipelineStatus> failedStatus = stepStatuses.stream()
                    .filter(ps -> ps.getStatus() == Status.FAILED)
                    .findFirst();

            Optional<PipelineStatus> inProgressStatus = stepStatuses.stream()
                    .filter(ps -> ps.getStatus() == Status.IN_PROGRESS)
                    .findFirst();

            Optional<PipelineStatus> pendingStatus = stepStatuses.stream()
                    .filter(ps -> ps.getStatus() == Status.PENDING)
                    .findFirst();

            String statusStr = "PENDING";
            Date stepStart = null;
            Date stepEnd = null;

            Status currentStatusEnum = latestStatusOpt.map(PipelineStatus::getStatus).orElse(Status.PENDING);

            if (currentStatusEnum == Status.COMPLETED && completedStatus.isPresent()) {
                statusStr = "COMPLETED";
                stepEnd = completedStatus.get().getCreatedAt();
                stepStart = inProgressStatus.map(PipelineStatus::getCreatedAt).orElse(stepEnd);
            } else if (currentStatusEnum == Status.FAILED && failedStatus.isPresent()) {
                statusStr = "FAILED";
                stepEnd = failedStatus.get().getCreatedAt();
                stepStart = inProgressStatus.map(PipelineStatus::getCreatedAt).orElse(stepEnd);
            } else if (currentStatusEnum == Status.IN_PROGRESS && inProgressStatus.isPresent()) {
                statusStr = "ACTIVE";
                stepStart = inProgressStatus.get().getCreatedAt();
                currentPhaseStartTime = stepStart;
            } else {
                statusStr = "PENDING";
            }

            if (stepStart != null) {
                if (overallStartTime == null || stepStart.before(overallStartTime)) {
                    overallStartTime = stepStart;
                }
            }
            if (stepEnd != null) {
                if (overallEndTime == null || stepEnd.after(overallEndTime)) {
                    overallEndTime = stepEnd;
                }
            }

            String startTimeFormatted = stepStart != null ? formatTime(stepStart) : null;
            String durationFormatted = formatDuration(stepStart, stepEnd);
            String outputText = formatOutputText(statusStr, durationFormatted);

            // Collect metrics from the most recent status entry for this step
            Map<String, String> stepMetrics = null;
            if (stepEnum == PipelineSteps.CONFIG) {
                stepMetrics = deriveConfigMetrics(pipelineConfig);
            } else {
                // Take metrics from completed or failed status, whichever is present
                Optional<PipelineStatus> metricSource = completedStatus.isPresent() ? completedStatus
                        : failedStatus.isPresent() ? failedStatus : inProgressStatus;
                stepMetrics = metricSource.map(PipelineStatus::getMetrics).orElse(null);
            }

            stepResponses.add(new PipelineStepExecutionResponse(
                    "step-" + stepEnum.getValue(),
                    getStepName(stepEnum),
                    statusStr,
                    startTimeFormatted,
                    durationFormatted,
                    outputText,
                    getStepDescription(stepEnum),
                    getStepIcon(stepEnum),
                    stepMetrics
            ));
        }

        String overallStatus = determineOverallStatus(stepResponses);
        Date totalEndTime = "ACTIVE".equals(overallStatus) || overallEndTime == null
                ? new Date()
                : overallEndTime;
        String totalExecutionTime = formatHms(overallStartTime, totalEndTime);
        String currentPhaseDuration = formatMs(currentPhaseStartTime, new Date());

        return new ExperimentExecutionResponse(
                pipelineConfig.getId(),
                pipelineConfig.getName(),
                overallStatus,
                totalExecutionTime,
                currentPhaseDuration,
                stepResponses
        );
    }

    private String determineOverallStatus(List<PipelineStepExecutionResponse> steps) {
        boolean hasFailed = steps.stream().anyMatch(s -> "FAILED".equals(s.status()));
        if (hasFailed) return "FAILED";

        boolean hasActive = steps.stream().anyMatch(s -> "ACTIVE".equals(s.status()));
        if (hasActive) return "ACTIVE";

        boolean allCompleted = steps.stream().allMatch(s -> "COMPLETED".equals(s.status()));
        if (allCompleted) return "COMPLETED";

        return "PENDING";
    }

    private Map<String, String> deriveConfigMetrics(PipelineConfig config) {
        Map<String, String> metrics = new LinkedHashMap<>();

        if (config.getExpertObjects() != null) {
            metrics.put("expertObjectsConfigured", String.valueOf(config.getExpertObjects().size()));
            String symbols = config.getExpertObjects().stream()
                    .map(ExpertObjectConfig::getSymbol)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            if (!symbols.isBlank()) metrics.put("expertObjectSymbols", symbols);
        }

        if (config.getLevels() != null) {
            metrics.put("searchLevels", String.valueOf(config.getLevels()));
        }
        if (config.getRetMax() > 0) {
            metrics.put("retMax", String.valueOf(config.getRetMax()));
        }
        if (config.getMaxComplexes() != null) {
            metrics.put("maxComplexes", String.valueOf(config.getMaxComplexes()));
        }
        metrics.put("useOnlyPrincipalName", String.valueOf(config.isUseOnlyPrincipalName()));

        TranscriptionFactorConfig tfConfig = config.getTranscriptionFactorConfig();
        if (tfConfig != null) {
            if (tfConfig.getSources() != null && !tfConfig.getSources().isEmpty()) {
                String sources = tfConfig.getSources().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                metrics.put("tfSources", sources);
            }
            if (tfConfig.getPromoterRegion() != null && !tfConfig.getPromoterRegion().isBlank()) {
                metrics.put("promoterRegion", tfConfig.getPromoterRegion());
            }
            if (tfConfig.getGenome() != null) {
                metrics.put("genome", tfConfig.getGenome().name());
            }
            if (tfConfig.getChromosome() != null && !tfConfig.getChromosome().isBlank()) {
                metrics.put("chromosome", tfConfig.getChromosome());
            }
        }

        return metrics.isEmpty() ? null : metrics;
    }

    private String getStepName(PipelineSteps step) {
        return switch (step) {
            case CONFIG -> "Configuration Setup";
            case LAUNCH -> "Launch Pipeline";
            case TRANSCRIPTION_FACTOR -> "Transcription Factor Config";
            case EXPERT_OBJECTS -> "Expert Objects Processing";
            case SEARCH_LEVELS -> "Search Levels Processing";
            case COMBINATIONS -> "Pubmed Combinations Generation";
            case SEARCH_PUBMED_IDS -> "Search PubMed IDs";
            case SEARCH_PUBTATOR -> "Search PubTator Annotations";
            case BUILD_KNOWLEDGE_BASE -> "Build Knowledge Base Graph";
            case GENERATE_ALIGNED_OBJECTS -> "Generate Aligned Objects";
            case UPDATE_ALIGNED_OBJECTS -> "Update Aligned Objects";
        };
    }

    private String getStepDescription(PipelineSteps step) {
        return switch (step) {
            case CONFIG -> "Configures initial pipeline parameters and network metadata.";
            case LAUNCH -> "Initiates execution and validates input payload parameters.";
            case TRANSCRIPTION_FACTOR -> "Queries and sets up transcription factor configurations.";
            case EXPERT_OBJECTS -> "Extracts and processes expert objects and target biological entities.";
            case SEARCH_LEVELS -> "Processes biological hierarchy levels and related object definitions.";
            case COMBINATIONS -> "Builds query combinations for literature and database integration.";
            case SEARCH_PUBMED_IDS -> "Queries PubMed API for relevant publication identifiers.";
            case SEARCH_PUBTATOR -> "Extracts bio-entity annotations using the PubTator engine.";
            case BUILD_KNOWLEDGE_BASE -> "Assembles the unified biological knowledge base network.";
            case GENERATE_ALIGNED_OBJECTS -> "Generates aligned objects and final output artifacts.";
            case UPDATE_ALIGNED_OBJECTS -> "Manual review and update of aligned biological objects.";
        };
    }

    private String getStepIcon(PipelineSteps step) {
        return switch (step) {
            case CONFIG -> "Sliders";
            case LAUNCH -> "GitBranch";
            case TRANSCRIPTION_FACTOR -> "Activity";
            case EXPERT_OBJECTS, SEARCH_LEVELS -> "Cpu";
            case COMBINATIONS, SEARCH_PUBMED_IDS, SEARCH_PUBTATOR -> "FileText";
            case BUILD_KNOWLEDGE_BASE -> "Activity";
            case GENERATE_ALIGNED_OBJECTS, UPDATE_ALIGNED_OBJECTS -> "FileText";
        };
    }

    private String formatTime(Date date) {
        if (date == null) return null;
        return date.toInstant().toString();
    }

    private String formatDuration(Date start, Date end) {
        if (start == null) return null;
        long endMillis = (end != null) ? end.getTime() : System.currentTimeMillis();
        long diffSecs = Math.max(0, (endMillis - start.getTime()) / 1000);
        long mins = diffSecs / 60;
        long secs = diffSecs % 60;
        if (mins > 0) {
            return mins + "m " + secs + "s";
        }
        return secs + "s";
    }

    private String formatOutputText(String status, String duration) {
        if ("COMPLETED".equals(status)) {
            return "Completed" + (duration != null ? " • " + duration : "");
        } else if ("ACTIVE".equals(status)) {
            return "(Active)";
        } else if ("FAILED".equals(status)) {
            return "Failed";
        }
        return "Pending";
    }

    private String formatHms(Date start, Date now) {
        if (start == null) return "00:00:00";
        long totalSecs = Math.max(0, (now.getTime() - start.getTime()) / 1000);
        long hrs = totalSecs / 3600;
        long mins = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    private String formatMs(Date start, Date now) {
        if (start == null) return "00:00";
        long totalSecs = Math.max(0, (now.getTime() - start.getTime()) / 1000);
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
