package com.biopatternsg.infrastructure.mongo_db.mappers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;

import java.util.List;

public class PipelineMapper {

    public static PipelineConfig toPipelineConfig(PipelineCollection pipelineCollection){

        return PipelineConfig.builder()
                .id(pipelineCollection.id.toString())
                .name(pipelineCollection.getName())
                .description(pipelineCollection.getDescription())
                .networkId(pipelineCollection.getNetworkId())
                .expertObjects(pipelineCollection.getExpertObjects())
                .transcriptionFactorConfig(pipelineCollection.getTranscriptionFactorConfig())
                .levels(pipelineCollection.getLevels())
                .build();
    }

    public static List<PipelineConfig> toPipelineConfigList(List<PipelineCollection> pipelineCollectionList){

        return pipelineCollectionList.stream().map(PipelineMapper::toPipelineConfig).toList();
    }
}
