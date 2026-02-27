package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRegistration;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserManagementUseCase implements UserManagement {

    private final KeycloakRepository keycloakRepository;

    @Override
    public void register(UserRegistration request) {

        var response = keycloakRepository.register(request);
        java.net.URI location = response.getLocation();
        String path = location.getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);
        keycloakRepository.verifyEmail(userId);
    }

    @Override
    public void recover(String userId) {

        keycloakRepository.recoveryPassword(userId);
    }
}
