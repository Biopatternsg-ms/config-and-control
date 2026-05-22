/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.models.UserRegister;
import com.biopatternsg.domain.models.user_registration.UserCredentials;
import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.dtos.UsersKeycloakFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;
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
                .email(request.username())
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .enabled(true)
                .emailVerified(false)
                .credentials(List.of(credentials))
                .requiredActions(List.of("VERIFY_EMAIL"))
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

    @Override
    public List<UserResponse> listUsers(UsersKeycloakFiltersRequest usersKeycloakFilters) {
        return keycloakRepository.listUsers(usersKeycloakFilters);
    }
}
