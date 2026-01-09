package com.biopatternsg.infrastructure.mongo_db.collections;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@MongoEntity(collection = "net")
public class PipelineCollection extends PanacheMongoEntity {

    private String name;
    private String description;
    private String networkId;
    private Integer levels;
    private List<ExpertObjectConfig> expertObjects;
    private TranscriptionFactorConfig transcriptionFactorConfig;
}
