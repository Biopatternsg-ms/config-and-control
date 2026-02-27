package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.infrastructure.dtos.keycloak.UserRegistration;
import jakarta.ws.rs.core.Response;

public interface KeycloakRepository {

    Response login(String user, String pass);
    Response register(UserRegistration userRegistration);
    void verifyEmail(String userId);
    void recoveryPassword(String userId);
}
