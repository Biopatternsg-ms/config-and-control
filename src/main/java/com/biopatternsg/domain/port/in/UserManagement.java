package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;

public interface UserManagement {

    void register(UserRequest request);
    void recover(String userId);
}
