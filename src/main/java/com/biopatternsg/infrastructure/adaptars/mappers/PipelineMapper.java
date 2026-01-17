package com.biopatternsg.infrastructure.adaptars.mappers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import org.bson.types.ObjectId;

import java.util.List;

public class PipelineMapper {

    public static PipelineConfig toPipelineConfig(PipelineCollection pipelineCollection){

        if(pipelineCollection == null){
            return null;
        }

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

    public static PipelineCollection toPipelineCollection(PipelineConfig pipelineConfig){

        if(pipelineConfig == null){
            return null;
        }

        var pipelineCollection = PipelineCollection.builder()
                .name(pipelineConfig.getName())
                .description(pipelineConfig.getDescription())
                .networkId(pipelineConfig.getNetworkId())
                .expertObjects(pipelineConfig.getExpertObjects())
                .transcriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig())
                .levels(pipelineConfig.getLevels())
                .build();

        if(pipelineConfig.getId() != null){
            pipelineCollection.id = new ObjectId(pipelineConfig.getId());
        }

        return pipelineCollection;
    }

    public static List<PipelineConfig> toPipelineConfigList(List<PipelineCollection> pipelineCollectionList){

        return pipelineCollectionList.stream().map(PipelineMapper::toPipelineConfig).toList();
    }
}
