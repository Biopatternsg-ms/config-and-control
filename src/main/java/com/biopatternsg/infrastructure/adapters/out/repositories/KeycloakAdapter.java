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

    public UserAuth login(String user, String pass) {

        try{
            return keycloakHttpClient.loginUser(grantType, clientId, clientSecret, user, pass, scope);
        } catch (WebApplicationException e) {
            log.info("Problemas con Keycloak: {}", e.getMessage());
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

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
                .credentials(
                        List.of(UserCredentialsRequest.builder()
                        .value(userConfig.getPassword())
                        .type("password")
                        .temporary(false)
                        .build()))
                .requiredActions(List.of("VERIFY_EMAIL"))
                .build();
        
        try{
            return keycloakHttpClient.register( accessToken, newUser);
        } catch (WebApplicationException e) {
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
    public List<UserConfig> listUsers(UserFilters userFilters) {

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();

        try{
            return keycloakHttpClient.usersList(accessToken, userFilters);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    public void verifyEmail(String userId){

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();
        List<String> actions = List.of("VERIFY_EMAIL");

        try{
            keycloakHttpClient.sendEmail(accessToken, userId, actions);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    @Override
    public void recoveryPassword(String username) {

        var credentials = keycloakHttpClient.loginClient(grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.getAccess_token();
        List<String> actions = List.of("UPDATE_PASSWORD");

        try{
            var filters = UserFilters.builder().username(username).build();
            var users = keycloakHttpClient.usersList(accessToken, filters);
            if (users != null && !users.isEmpty()) {
                var firstUser = users.getFirst();
                if (firstUser != null && firstUser.getId() != null && !firstUser.getId().isBlank()) {
                    keycloakHttpClient.sendEmail(accessToken, firstUser.getId(), actions);
                }
            }
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

}
