package com.biopatternsg.infrastructure.adaptars.mappers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import org.bson.types.ObjectId;

import java.util.List;

public class NetworkMapper {

    public static NetworkConfig toNetworkConfig(NetworkCollection networkCollection){

        if(networkCollection == null){
            return null;
        }

        return NetworkConfig.builder()
                .id(networkCollection.id.toString())
                .userId(networkCollection.getUserId())
                .name(networkCollection.getName())
                .description(networkCollection.getDescription())
                .createdAt(networkCollection.id.getTimestamp())
                .build();
    }

    public static NetworkCollection toNetworkCollection(NetworkConfig networkConfig, String userId){

        if(networkConfig == null){
            return null;
        }

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

    public static List<NetworkConfig> toNetworkConfigList(List<NetworkCollection> collectionList){

        return collectionList.stream().map(NetworkMapper::toNetworkConfig).toList();
    }
}
