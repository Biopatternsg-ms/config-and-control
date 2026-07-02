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

import com.biopatternsg.domain.port.in.UserAuthentication;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.domain.models.UserAuth;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserAuthenticationUseCase implements UserAuthentication {

    private final KeycloakRepository keycloakRepository;

    @Override
    public UserAuth login(String user, String pass) {

        return keycloakRepository.login(user, pass);
    }

    @Override
    public UserAuth refreshToken(String refreshToken) {

        return keycloakRepository.refreshToken(refreshToken);
    }

    @Override
    public Response recoveryPassword(String email) {

        keycloakRepository.recoveryPassword(email);
        return Response.ok().build();
    }
}
