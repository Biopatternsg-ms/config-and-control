package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.UsersKeycloakFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;

import java.util.List;

public interface UserManagement {

    void register(UserRequest request);
    void recover(String userId);
    List<UserResponse> listUsers(UsersKeycloakFiltersRequest usersKeycloakFilters);
}
