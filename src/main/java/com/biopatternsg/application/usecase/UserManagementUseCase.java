package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.UserRegister;
import com.biopatternsg.domain.models.user_registration.UserCredentials;
import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class UserManagementUseCase implements UserManagement {

    private final KeycloakRepository keycloakRepository;

    @Override
    public void register(UserRequest request) {

        var credentials = UserCredentials.builder()
                .value(request.password())
                .type("password")
                .temporary(false)
                .build();

        var newUser = UserRegister.builder()
                .email(request.email())
                .username(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .enabled(true)
                .emailVerified(false)
                .credentials(List.of(credentials))
                .build();

        var response = keycloakRepository.register(newUser);
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
