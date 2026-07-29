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

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.domain.port.out.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class UserManagementUseCase implements UserManagement {

    private final UserRepository userRepository;
    private final KeycloakRepository keycloakRepository;

    @Override
    public void register(UserConfig userConfig) {

        var response = keycloakRepository.register(userConfig);
        java.net.URI location = response.getLocation();
        String path = location.getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);

        userConfig.setIdentityProviderId(userId);
        userRepository.create(userConfig);

        keycloakRepository.sendEmail(userId, List.of("VERIFY_EMAIL"));
    }

    @Override
    public void syncUsers(){
        var keycloakList = keycloakRepository.listUsers(UserConfig.builder().build());
        var apiRestList = userRepository.list(UserConfig.builder().build(), 0, 0).list();

        var apiRestIds = apiRestList.stream()
                .map(UserConfig::getIdentityProviderId)
                .collect(java.util.stream.Collectors.toSet());

        for (UserConfig keycloakUser : keycloakList) {
            if (!apiRestIds.contains(keycloakUser.getId())) {
                userRepository.create(keycloakUser);
            }
        }
    }

    @Override
    public ReportFormat<UserConfig> listUsers(UserConfig filters, int page, int size) {
        return userRepository.list(filters, page, size);
    }

    @Override
    public void recoveryPassword(String email){
        var user = userRepository.find(email);
        if(user != null){
            keycloakRepository.sendEmail(user.getIdentityProviderId(), List.of("UPDATE_PASSWORD"));
        }
    }

    @Override
    public UserConfig updateStatus(String id, boolean enabled) {

        var user = userRepository.findById(id);
        if (user == null) {
            throw new UnprocessableEntityException("The user doesn't exist");
        }

        user.setEnabled(enabled);
        keycloakRepository.updateEnabled(user.getIdentityProviderId(), enabled);
        userRepository.update(user);

        return user;
    }
}
