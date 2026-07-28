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

import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.infrastructure.dtos.UserFiltersRequest;
import com.biopatternsg.infrastructure.dtos.UserRequest;
import com.biopatternsg.infrastructure.dtos.UserResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserKeycloakResponse;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;

import java.util.List;

public class UserMapper {

    public static UserConfig requestToModel(UserRequest userRequest){

        if (userRequest == null) return null;
        return UserConfig.builder()
                .username(userRequest.username())
                .email(userRequest.username())
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .build();
    }

    public static UserResponse modelToResponse(UserConfig userConfig){

        if (userConfig == null) return null;
        return UserResponse.builder()
                .id(userConfig.getId())
                .username(userConfig.getUsername())
                .firstName(userConfig.getFirstName())
                .lastName(userConfig.getLastName())
                .enabled(userConfig.isEnabled())
                .createdAt(userConfig.getCreatedAt())
                .build();
    }

    public static List<UserResponse> modelToResponseList(List<UserConfig> userList){

        return userList.stream().map(UserMapper::modelToResponse).toList();
    }

    public static UserCollection modelToCollection(UserConfig userConfig) {

        if (userConfig == null) return null;
        return UserCollection.builder()
                .identityProviderId(userConfig.getIdentityProviderId())
                .username(userConfig.getUsername())
                .firstName(userConfig.getFirstName())
                .lastName(userConfig.getLastName())
                .enabled(userConfig.isEnabled())
                .build();
    }

    public static UserConfig collectionToModel(UserCollection collection) {

        if (collection == null) return null;
        return UserConfig.builder()
                .id(collection.id.toString())
                .identityProviderId(collection.getIdentityProviderId())
                .username(collection.getUsername())
                .firstName(collection.getFirstName())
                .lastName(collection.getLastName())
                .enabled(collection.isEnabled())
                .createdAt(collection.id.getTimestamp())
                .build();
    }

    public static UserFilters filtersToModel(UserFiltersRequest userFilters){

        if (userFilters == null) return null;
        return UserFilters.builder()
                .username(userFilters.username())
                .firstName(userFilters.firstName())
                .lastName(userFilters.lastName())
                .enable(userFilters.enable())
                .build();
    }

    public static UserConfig keycloakToModel(UserKeycloakResponse userKeycloakResponse){

        if (userKeycloakResponse == null) return null;
        return UserConfig.builder()
                .identityProviderId(userKeycloakResponse.id())
                .username(userKeycloakResponse.username())
                .firstName(userKeycloakResponse.firstName())
                .lastName(userKeycloakResponse.lastName())
                .enabled(userKeycloakResponse.enabled())
                .build();
    }

    public static List<UserConfig> keycloakToModelList(List<UserKeycloakResponse> identifiersList){

        return identifiersList.stream().map(UserMapper::keycloakToModel).toList();
    }
}
