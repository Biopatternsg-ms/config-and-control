package com.biopatternsg.infrastructure.mongo_db.mappers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;

public class NetworkMapper {

    public static NetworkConfig toNetworkConfig(NetworkCollection networkCollection){

        return NetworkConfig.builder()
                .id(networkCollection.id.toString())
                .userId(networkCollection.getUserId())
                .name(networkCollection.getName())
                .description(networkCollection.getDescription())
                .build();
    }
}
