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

import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.domain.port.out.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class UserManagementUseCase implements UserManagement {

    private final KeycloakRepository keycloakRepository;
    private final UserRepository userRepository;

    @Override
    public void register(UserConfig userConfig) {

        var response = keycloakRepository.register(userConfig);
        userRepository.create(userConfig);
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
    public void syncUsers(){
        var keycloakList = keycloakRepository.listUsers(UserFilters.builder().build());
        var apiRestList = userRepository.list(UserFilters.builder().build(), 0, 0);

        var apiRestIds = apiRestList.stream()
                .map(UserConfig::getId)
                .collect(java.util.stream.Collectors.toSet());

        for (UserConfig keycloakUser : keycloakList) {
            if (keycloakUser.getId() != null && !apiRestIds.contains(keycloakUser.getId())) {
                userRepository.create(keycloakUser);
            }
        }
    }

    @Override
    public List<UserConfig> listUsers(UserFilters userFilters, int page, int size) {
        return userRepository.list(userFilters, page, size);
    }
}
