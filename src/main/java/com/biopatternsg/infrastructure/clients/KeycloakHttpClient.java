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
package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.models.UserFilters;
import com.biopatternsg.domain.models.UserAuth;
import com.biopatternsg.infrastructure.dtos.keycloak.IntrospectResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.RoleResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRegisterRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "keycloak-auth-api")
public interface KeycloakHttpClient {

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    UserAuth loginClient(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret
    );

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    UserAuth loginUser(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("scope") String scope
    );

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    UserAuth refreshToken(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void logout(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    IntrospectResponse introspectToken(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("token") String token
    );

    @POST
    @Path("/admin/realms/biopatternsg/users")
    @Produces(MediaType.APPLICATION_JSON)
    Response register(
            @HeaderParam("Authorization") String token,
            UserRegisterRequest userRegister
    );

    @GET
    @Path("admin/realms/biopatternsg/users")
    List<UserConfig> usersList(
            @HeaderParam("Authorization") String token,
            @BeanParam UserFilters usersKeycloakFilters
    );

    @PUT
    @Path("/admin/realms/biopatternsg/users/{userId}/execute-actions-email")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendEmail(
            @HeaderParam("Authorization") String token,
            @PathParam("userId") String userId,
            List<String> actions
    );

    @GET
    @Path("/admin/realms/biopatternsg/roles")
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleResponse> getRoles(
            @HeaderParam("Authorization") String token
    );

    @POST
    @Path("/admin/realms/biopatternsg/users/{userId}/role-mappings/realm")
    @Consumes(MediaType.APPLICATION_JSON)
    void setRoles(
            @HeaderParam("Authorization") String token,
            @PathParam("userId") String userId,
            List<RoleResponse> roles
    );
}
