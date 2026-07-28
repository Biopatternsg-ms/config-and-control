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
package com.biopatternsg.infrastructure.adapters.out.repositories;

import com.biopatternsg.domain.exceptions.KeycloakServiceException;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.adapters.mappers.UserMapper;
import com.biopatternsg.infrastructure.clients.KeycloakHttpClient;
import com.biopatternsg.domain.models.UserAuth;
import com.biopatternsg.infrastructure.dtos.keycloak.UserCredentialsRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRegisterRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Slf4j
@ApplicationScoped
public class KeycloakAdapter implements KeycloakRepository {

    @Inject
    @RestClient
    KeycloakHttpClient keycloakHttpClient;

    @ConfigProperty(name = "keycloak.client.id")
    String clientId;
    @ConfigProperty(name = "keycloak.client.secret")
    String clientSecret;
    @ConfigProperty(name = "keycloak.scope")
    String scope;
    @ConfigProperty(name = "keycloak.grant-type")
    String grantType;
    @ConfigProperty(name = "keycloak.grant-type-client")
    String grantTypeClient;

    @Override
    public UserAuth login(String user, String pass) {

        try{
            return keycloakHttpClient.loginUser(grantType, clientId, clientSecret, user, pass, scope);
        } catch (WebApplicationException e) {
            log.info("Problemas con Keycloak: {}", e.getMessage());
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    @Override
    public UserAuth refreshToken(String refreshToken) {

        try{
            return keycloakHttpClient.refreshToken("refresh_token", clientId, clientSecret, refreshToken);
        } catch (WebApplicationException e) {
            log.info("Problemas con Keycloak: {}", e.getMessage());
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    @Override
    public Response register(UserConfig userConfig) {

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();

        var newUser = UserRegisterRequest.builder()
                .email(userConfig.getUsername())
                .username(userConfig.getUsername())
                .firstName(userConfig.getFirstName())
                .lastName(userConfig.getLastName())
                .enabled(true)
                .emailVerified(false)
                .requiredActions(List.of("UPDATE_PASSWORD"))
                .build();
        
        try{
            Response response = keycloakHttpClient.register(accessToken, newUser);
            if (response.getStatus() == 201) {
                java.net.URI location = response.getLocation();
                if (location != null) {
                    String path = location.getPath();
                    String userId = path.substring(path.lastIndexOf('/') + 1);
                    
                    var roles = keycloakHttpClient.getRoles(accessToken);
                    if (roles != null) {
                        var researcherRole = roles.stream()
                                .filter(r -> "researcher".equals(r.getName()))
                                .findFirst();
                        researcherRole.ifPresent(roleResponse ->
                                keycloakHttpClient.setRoles(accessToken, userId, List.of(roleResponse)));
                    }
                }
            }
            
            return response;
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    @Override
    public List<UserConfig> listUsers(UserFilters userFilters) {

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();

        try{
            var usersKeycloak = keycloakHttpClient.usersList(accessToken, userFilters);
            return UserMapper.keycloakToModelList(usersKeycloak);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    public void sendEmail(String userId, List<String> actions){

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();

        try{
            keycloakHttpClient.sendEmail(accessToken, userId, actions);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }
}
