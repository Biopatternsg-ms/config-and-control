package com.biopatternsg.infrastructure.mongo_db.collections;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@MongoEntity(collection = "network")
public class NetworkCollection extends PanacheMongoEntity {

    Long userId;
    String name;
    String description;
}
