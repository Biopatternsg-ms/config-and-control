package com.biopatternsg.domain.port.in;

import com.biopatternsg.infrastructure.dtos.keycloak.UserRegistration;

public interface UserManagement {

    void register(UserRegistration request);
    void recover(String userId);
}
