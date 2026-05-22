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
package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.UserRegister;
import com.biopatternsg.infrastructure.dtos.UsersKeycloakFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;
import jakarta.ws.rs.core.Response;

import java.util.List;

public interface KeycloakRepository {

    Response login(String user, String pass);
    Response register(UserRegister userRegister);
    List<UserResponse> listUsers(UsersKeycloakFiltersRequest usersKeycloakFilters);
    void verifyEmail(String userId);
    void recoveryPassword(String userId);
}
