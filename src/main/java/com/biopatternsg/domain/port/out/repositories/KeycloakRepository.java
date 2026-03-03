package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.UserRegister;
import jakarta.ws.rs.core.Response;

public interface KeycloakRepository {

    Response login(String user, String pass);
    Response register(UserRegister userRegister);
    void verifyEmail(String userId);
    void recoveryPassword(String userId);
}
