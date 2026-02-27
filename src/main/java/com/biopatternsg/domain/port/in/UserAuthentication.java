package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.keycloak.Response;

public interface UserAuthentication {

    Response login(String user, String pass);
}
