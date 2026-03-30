package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.UserRegister;
import com.biopatternsg.infrastructure.dtos.UsersKeycloakFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;
import jakarta.ws.rs.core.Response;

import java.util.List;

public interface KeycloakRepository {

    Response login(String user, String pass);
    Response register(UserRegister userRegister);
    List<UserResponse> listUsers(UsersKeycloakFiltersRequest usersKeycloakFilters);
    void verifyEmail(String userId);
    void recoveryPassword(String userId);
}
