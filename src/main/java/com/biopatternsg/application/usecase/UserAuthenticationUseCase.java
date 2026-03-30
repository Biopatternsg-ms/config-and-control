package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.port.in.UserAuthentication;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.dtos.keycloak.LoginClientResponse;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserAuthenticationUseCase implements UserAuthentication {

    private final KeycloakRepository keycloakRepository;

    @Override
    public LoginClientResponse login(String user, String pass) {

        var response = keycloakRepository.login(user, pass);
        return response.readEntity(LoginClientResponse.class);
    }
}
