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

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.dtos.NetworkResponse;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import org.bson.types.ObjectId;

import java.util.List;

public final class NetworkMapper {

    private NetworkMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static NetworkConfig collectionToModel(NetworkCollection networkCollection) {
        if (networkCollection == null) {
            return null;
        }

        return NetworkConfig.builder()
                .id(networkCollection.id != null ? networkCollection.id.toString() : null)
                .userId(networkCollection.getUserId())
                .name(networkCollection.getName())
                .description(networkCollection.getDescription())
                .createdAt(networkCollection.id != null ? networkCollection.id.getTimestamp() : 0)
                .build();
    }

    public static NetworkResponse modelToResponse(NetworkConfig networkConfig) {
        if (networkConfig == null) {
            return null;
        }

        return NetworkResponse.builder()
                .id(networkConfig.getId())
                .userId(networkConfig.getUserId())
                .name(networkConfig.getName())
                .description(networkConfig.getDescription())
                .createdAt(networkConfig.getCreatedAt())
                .build();
    }

    public static NetworkCollection modelToCollection(NetworkConfig networkConfig, String userId) {
        if (networkConfig == null) {
            return null;
        }

        var networkCollection = NetworkCollection.builder()
                .userId(userId)
                .name(networkConfig.getName())
                .description(networkConfig.getDescription())
                .build();

        if (networkConfig.getId() != null && ObjectId.isValid(networkConfig.getId())) {
            networkCollection.id = new ObjectId(networkConfig.getId());
        }
        return networkCollection;
    }

    public static List<NetworkConfig> collectionToModelList(List<NetworkCollection> collectionList) {
        if (collectionList == null) {
            return List.of();
        }
        return collectionList.stream().map(NetworkMapper::collectionToModel).toList();
    }

    public static List<NetworkResponse> modelToResponseList(List<NetworkConfig> networkConfigList) {
        if (networkConfigList == null) {
            return List.of();
        }
        return networkConfigList.stream().map(NetworkMapper::modelToResponse).toList();
    }

    public static ReportFormat<NetworkConfig> toDomainReport(ReportFormat<NetworkCollection> reportCollection) {
        if (reportCollection == null) {
            return new ReportFormat<>(0L, List.of());
        }
        List<NetworkConfig> modelList = reportCollection.list() != null
                ? reportCollection.list().stream().map(NetworkMapper::collectionToModel).toList()
                : List.of();
        return new ReportFormat<>(reportCollection.count(), modelList);
    }

    public static ReportFormat<NetworkResponse> toReportResponse(ReportFormat<NetworkConfig> modelFormat) {
        if (modelFormat == null) {
            return new ReportFormat<>(0L, List.of());
        }
        List<NetworkResponse> responseList = modelFormat.list() != null
                ? modelFormat.list().stream().map(NetworkMapper::modelToResponse).toList()
                : List.of();
        return new ReportFormat<>(modelFormat.count(), responseList);
    }

    public static NetworkConfig requestToModel(CreateNetworkRequest createNetwork) {
        if (createNetwork == null) {
            return null;
        }
        return NetworkConfig.builder()
                .name(createNetwork.name())
                .description(createNetwork.description())
                .build();
    }

    public static NetworkConfig requestToModel(UpdateNetworkRequest updateNetwork) {
        if (updateNetwork == null) {
            return null;
        }
        return NetworkConfig.builder()
                .id(updateNetwork.id())
                .name(updateNetwork.name())
                .description(updateNetwork.description())
                .build();
    }

    public static NetworkConfig requestToModel(FindNetworkRequest findNetwork) {
        if (findNetwork == null) {
            return null;
        }
        return NetworkConfig.builder()
                .id(findNetwork.id())
                .name(findNetwork.name())
                .description(findNetwork.description())
                .build();
    }

    // Backward-compatibility delegating aliases
    public static NetworkConfig toNetworkConfig(NetworkCollection networkCollection) {
        return collectionToModel(networkCollection);
    }

    public static NetworkResponse toNetworkResponse(NetworkConfig networkConfig) {
        return modelToResponse(networkConfig);
    }

    public static NetworkCollection toNetworkCollection(NetworkConfig networkConfig, String userId) {
        return modelToCollection(networkConfig, userId);
    }

    public static List<NetworkConfig> toNetworkConfigList(List<NetworkCollection> collectionList) {
        return collectionToModelList(collectionList);
    }

    public static List<NetworkResponse> toNetworkResponseList(List<NetworkConfig> networkConfigList) {
        return modelToResponseList(networkConfigList);
    }
}
