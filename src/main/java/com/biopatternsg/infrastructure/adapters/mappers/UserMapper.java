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
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;

import java.util.List;

public class UserMapper {

    public static UserConfig userRequestToModel(UserRequest newUser){

        return UserConfig.builder()
                .username(newUser.username())
                .email(newUser.username())
                .firstName(newUser.firstName())
                .lastName(newUser.lastName())
                .password(newUser.password())
                .build();
    }

    public static UserFilters filtersRequestToModel(UserFiltersRequest userFilters){

        return UserFilters.builder()
                .username(userFilters.username())
                .email(userFilters.email())
                .search(userFilters.search())
                .enable(userFilters.enable())
                .build();
    }

    public static UserResponse toResponse(UserConfig userConfig){

        return UserResponse.builder()
                .id(userConfig.getId())
                .username(userConfig.getUsername())
                .email(userConfig.getEmail())
                .firstName(userConfig.getFirstName())
                .lastName(userConfig.getLastName())
                .enabled(userConfig.getEnabled())
                .emailVerified(userConfig.getEmailVerified())
                .createdAt(userConfig.getCreatedAt())
                .build();
    }

    public static List<UserResponse> modelToResponseList(List<UserConfig> userList){

        return userList.stream().map(UserMapper::toResponse).toList();
    }
}
