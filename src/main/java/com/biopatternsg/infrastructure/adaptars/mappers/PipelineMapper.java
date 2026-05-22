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
package com.biopatternsg.infrastructure.adaptars.mappers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.infrastructure.dtos.PipelineResponse;
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
                .networkId(pipelineCollection.getNetworkId())
                .description(pipelineCollection.getDescription())
                .step(pipelineCollection.getStep())
                .levels(pipelineCollection.getLevels())
                .statuses(pipelineCollection.getStatuses())
                .expertObjects(pipelineCollection.getExpertObjects())
                .transcriptionFactorConfig(pipelineCollection.getTranscriptionFactorConfig())
                .createdAt(pipelineCollection.id.getTimestamp())
                .build();
    }

    public static PipelineResponse toPipelineResponse(PipelineCollection pipelineCollection){

        if(pipelineCollection == null){
            return null;
        }

        return PipelineResponse.builder()
                .id(pipelineCollection.id.toString())
                .name(pipelineCollection.getName())
                .description(pipelineCollection.getDescription())
                .step(pipelineCollection.getStep())
                .createdAt(pipelineCollection.id.getTimestamp())
                .build();
    }

    public static PipelineCollection toPipelineCollection(PipelineConfig pipelineConfig){

        if(pipelineConfig == null){
            return null;
        }

        var pipelineCollection = PipelineCollection.builder()
                .name(pipelineConfig.getName())
                .networkId(pipelineConfig.getNetworkId())
                .description(pipelineConfig.getDescription())
                .step(pipelineConfig.getStep())
                .statuses(pipelineConfig.getStatuses())
                .levels(pipelineConfig.getLevels())
                .expertObjects(pipelineConfig.getExpertObjects())
                .transcriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig())
                .build();

        if(pipelineConfig.getId() != null){
            pipelineCollection.id = new ObjectId(pipelineConfig.getId());
        }

        return pipelineCollection;
    }

    public static List<PipelineConfig> toPipelineConfigList(List<PipelineCollection> pipelineCollectionList){

        return pipelineCollectionList.stream().map(PipelineMapper::toPipelineConfig).toList();
    }

    public static List<PipelineResponse> toPipelineResponseList(List<PipelineCollection> pipelineCollectionList){

        return pipelineCollectionList.stream().map(PipelineMapper::toPipelineResponse).toList();
    }
}
