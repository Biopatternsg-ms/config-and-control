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
