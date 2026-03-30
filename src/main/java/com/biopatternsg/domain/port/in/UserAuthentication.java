package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.keycloak.LoginClientResponse;

public interface UserAuthentication {

    LoginClientResponse login(String user, String pass);
}
