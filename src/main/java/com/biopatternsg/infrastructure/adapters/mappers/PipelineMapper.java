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
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.PipelineResumeSummary;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.dtos.*;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import org.bson.types.ObjectId;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PipelineMapper {

    private PipelineMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static PipelineConfig collectionToModel(PipelineCollection pipelineCollection) {
        if (pipelineCollection == null) {
            return null;
        }

        return PipelineConfig.builder()
                .id(pipelineCollection.id != null ? pipelineCollection.id.toString() : null)
                .name(pipelineCollection.getName())
                .networkId(pipelineCollection.getNetworkId())
                .description(pipelineCollection.getDescription())
                .step(pipelineCollection.getStep())
                .levels(pipelineCollection.getLevels())
                .retMax(pipelineCollection.getRetMax() != null ? pipelineCollection.getRetMax() : 0)
                .maxComplexes(pipelineCollection.getMaxComplexes())
                .statuses(pipelineCollection.getStatuses())
                .expertObjects(pipelineCollection.getExpertObjects())
                .alignedExpertObjects(pipelineCollection.getAlignedExpertObjects())
                .transcriptionFactorConfig(pipelineCollection.getTranscriptionFactorConfig())
                .useOnlyPrincipalName(pipelineCollection.getUseOnlyPrincipalName() != null ? pipelineCollection.getUseOnlyPrincipalName() : true)
                .createdAt(pipelineCollection.id != null ? pipelineCollection.id.getTimestamp() : 0)
                .build();
    }

    public static PipelineResponse modelToResponse(PipelineConfig pipelineConfig) {
        if (pipelineConfig == null) {
            return null;
        }

        return PipelineResponse.builder()
                .id(pipelineConfig.getId())
                .networkId(pipelineConfig.getNetworkId())
                .name(pipelineConfig.getName())
                .description(pipelineConfig.getDescription())
                .step(pipelineConfig.getStep())
                .createdAt(pipelineConfig.getCreatedAt())
                .status(getMostRecentStatus(pipelineConfig.getStatuses()))
                .build();
    }

    public static PipelineDetailResponse modelToDetailResponse(PipelineConfig pipelineConfig) {
        if (pipelineConfig == null) {
            return null;
        }

        return PipelineDetailResponse.builder()
                .id(pipelineConfig.getId())
                .name(pipelineConfig.getName())
                .description(pipelineConfig.getDescription())
                .networkId(pipelineConfig.getNetworkId())
                .levels(pipelineConfig.getLevels())
                .step(pipelineConfig.getStep())
                .statuses(pipelineConfig.getStatuses() != null ? List.copyOf(pipelineConfig.getStatuses()) : List.of())
                .status(getMostRecentStatus(pipelineConfig.getStatuses()))
                .expertObjects(pipelineConfig.getExpertObjects() != null ? List.copyOf(pipelineConfig.getExpertObjects()) : List.of())
                .alignedExpertObjects(pipelineConfig.getAlignedExpertObjects() != null ? List.copyOf(pipelineConfig.getAlignedExpertObjects()) : List.of())
                .transcriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig())
                .useOnlyPrincipalName(pipelineConfig.isUseOnlyPrincipalName())
                .createdAt(pipelineConfig.getCreatedAt())
                .retMax(pipelineConfig.getRetMax())
                .maxComplexes(pipelineConfig.getMaxComplexes())
                .build();
    }

    public static PipelineCollection modelToCollection(PipelineConfig pipelineConfig) {
        if (pipelineConfig == null) {
            return null;
        }

        var pipelineCollection = PipelineCollection.builder()
                .name(pipelineConfig.getName())
                .networkId(pipelineConfig.getNetworkId())
                .description(pipelineConfig.getDescription())
                .step(pipelineConfig.getStep())
                .statuses(pipelineConfig.getStatuses())
                .levels(pipelineConfig.getLevels())
                .retMax(pipelineConfig.getRetMax())
                .maxComplexes(pipelineConfig.getMaxComplexes())
                .useOnlyPrincipalName(pipelineConfig.isUseOnlyPrincipalName())
                .expertObjects(pipelineConfig.getExpertObjects())
                .alignedExpertObjects(pipelineConfig.getAlignedExpertObjects())
                .transcriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig())
                .build();

        if (pipelineConfig.getId() != null && ObjectId.isValid(pipelineConfig.getId())) {
            pipelineCollection.id = new ObjectId(pipelineConfig.getId());
        }

        return pipelineCollection;
    }

    public static List<PipelineConfig> collectionToModelList(List<PipelineCollection> collectionList) {
        if (collectionList == null) {
            return List.of();
        }
        return collectionList.stream().map(PipelineMapper::collectionToModel).toList();
    }

    public static List<PipelineResponse> modelToResponseList(List<PipelineConfig> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream().map(PipelineMapper::modelToResponse).toList();
    }

    public static ExpertObjectConfig symbolToExpertObject(String symbol) {
        if (symbol == null) {
            return null;
        }
        ExpertObjectConfig config = new ExpertObjectConfig();
        config.setSymbol(symbol);
        return config;
    }

    public static List<ExpertObjectConfig> symbolsToExpertObjects(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }
        return symbols.stream()
                .filter(Objects::nonNull)
                .map(PipelineMapper::symbolToExpertObject)
                .toList();
    }

    public static ReportFormat<PipelineConfig> toDomainReport(ReportFormat<PipelineCollection> reportCollection) {
        if (reportCollection == null) {
            return new ReportFormat<>(0L, List.of());
        }
        List<PipelineConfig> modelList = reportCollection.list() != null
                ? reportCollection.list().stream().map(PipelineMapper::collectionToModel).toList()
                : List.of();
        return new ReportFormat<>(reportCollection.count(), modelList);
    }

    public static ReportFormat<PipelineResponse> toReportResponse(ReportFormat<PipelineConfig> modelFormat) {
        if (modelFormat == null) {
            return new ReportFormat<>(0L, List.of());
        }
        List<PipelineResponse> responseList = modelFormat.list() != null
                ? modelFormat.list().stream().map(PipelineMapper::modelToResponse).toList()
                : List.of();
        return new ReportFormat<>(modelFormat.count(), responseList);
    }

    private static PipelineStatus getMostRecentStatus(List<PipelineStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream()
                .filter(Objects::nonNull)
                .filter(status -> status.getCreatedAt() != null)
                .max(Comparator.comparing(PipelineStatus::getCreatedAt))
                .orElse(null);
    }

    public static PipelineConfig requestToCreate(CreatePipelineRequest createPipeline) {
        if (createPipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .name(createPipeline.name())
                .description(createPipeline.description())
                .networkId(createPipeline.networkId())
                .build();
    }

    public static PipelineConfig requestToModel(CreatePipelineRequest createPipeline) {
        return requestToCreate(createPipeline);
    }

    public static PipelineConfig requestToUpdate(UpdatePipelineDescriptionRequest updatePipeline) {
        if (updatePipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(updatePipeline.id())
                .name(updatePipeline.name())
                .description(updatePipeline.description())
                .build();
    }

    public static PipelineConfig requestToUpdate(UpdatePipelineTranscriptionFactorRequest updatePipeline) {
        if (updatePipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(updatePipeline.id())
                .transcriptionFactorConfig(updatePipeline.transcriptionFactorConfig())
                .build();
    }

    public static PipelineConfig requestToUpdate(UpdatePipelineExpertObjectsRequest updatePipeline) {
        if (updatePipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(updatePipeline.id())
                .expertObjects(updatePipeline.expertObjects())
                .build();
    }

    public static PipelineConfig requestToUpdate(UpdateAlignedExpertObjectsRequest updatePipeline) {
        if (updatePipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(updatePipeline.id())
                .alignedExpertObjects(updatePipeline.alignedExpertObjects())
                .build();
    }

    public static PipelineConfig requestToUpdate(UpdatePipelineSearchConfigRequest updatePipeline) {
        if (updatePipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(updatePipeline.id())
                .levels(updatePipeline.levels())
                .retMax(updatePipeline.retMax())
                .maxComplexes(updatePipeline.maxComplexes())
                .useOnlyPrincipalName(updatePipeline.useOnlyPrincipalName() != null ? updatePipeline.useOnlyPrincipalName() : true)
                .build();
    }

    public static PipelineConfig requestToUpdate(FindPipelineRequest findPipeline) {
        if (findPipeline == null) {
            return null;
        }
        return PipelineConfig.builder()
                .id(findPipeline.id())
                .networkId(findPipeline.networkId())
                .name(findPipeline.name())
                .description(findPipeline.description())
                .build();
    }

    public static PipelineConfig requestToModel(FindPipelineRequest findPipeline) {
        return requestToUpdate(findPipeline);
    }

    public static PipelineStatus requestToStatus(PipelineStepRequest pipelineStepRequest) {
        if (pipelineStepRequest == null) {
            return null;
        }
        return PipelineStatus.builder()
                .step(pipelineStepRequest.step())
                .status(pipelineStepRequest.status())
                .metrics(pipelineStepRequest.metrics())
                .build();
    }

    public static PipelineResumeResponse toResponse(PipelineResumeSummary summary) {
        if (summary == null) {
            return null;
        }
        return new PipelineResumeResponse(
                summary.getMicroService(),
                summary.getTotalFound(),
                summary.getResumedCount(),
                summary.getFailedCount(),
                summary.getResumedPipelineIds(),
                summary.getFailedPipelineIds(),
                summary.getErrors()
        );
    }

    // Backward-compatibility delegating aliases
    public static PipelineConfig collectionToConfig(PipelineCollection pipelineCollection) {
        return collectionToModel(pipelineCollection);
    }

    public static PipelineResponse configToResponse(PipelineConfig pipelineConfig) {
        return modelToResponse(pipelineConfig);
    }

    public static PipelineDetailResponse configToDetailResponse(PipelineConfig pipelineConfig) {
        return modelToDetailResponse(pipelineConfig);
    }

    public static PipelineCollection configToCollection(PipelineConfig pipelineConfig) {
        return modelToCollection(pipelineConfig);
    }

    public static List<PipelineConfig> collectionToConfigList(List<PipelineCollection> pipelineCollectionList) {
        return collectionToModelList(pipelineCollectionList);
    }

    public static List<PipelineResponse> collectionToResponseList(List<PipelineConfig> pipelineCollectionList) {
        return modelToResponseList(pipelineCollectionList);
    }
}
