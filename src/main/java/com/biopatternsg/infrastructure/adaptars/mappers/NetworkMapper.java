package com.biopatternsg.infrastructure.adaptars.mappers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import org.bson.types.ObjectId;

public class NetworkMapper {

    public static NetworkConfig toNetworkConfig(NetworkCollection networkCollection){

        return NetworkConfig.builder()
                .id(networkCollection.id.toString())
                .userId(networkCollection.getUserId())
                .name(networkCollection.getName())
                .description(networkCollection.getDescription())
                .build();
    }

    public static NetworkCollection toNetworkCollection(NetworkConfig networkConfig, Long userId){

        var networkCollection = NetworkCollection.builder()
                .userId(userId)
                .name(networkConfig.getName())
                .description(networkConfig.getDescription())
                .build();

        if(networkConfig.getId() != null){
            networkCollection.id = new ObjectId(networkConfig.getId());
        }
        return networkCollection;
    }
}
