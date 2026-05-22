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
