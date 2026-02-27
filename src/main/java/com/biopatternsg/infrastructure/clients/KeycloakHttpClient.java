package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.infrastructure.dtos.keycloak.IntrospectResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRegistration;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "keycloak-auth-api")
public interface KeycloakHttpClient {

    @POST
    @Path("/realms/{realmName}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    com.biopatternsg.infrastructure.dtos.keycloak.Response loginClient(
            @PathParam("realmName") String realmName,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret
    );

    @POST
    @Path("/realms/{realmName}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response loginUser(
            @PathParam("realmName") String realmName,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("scope") String scope
    );

    @POST
    @Path("/admin/realms/{realmName}/users")
    @Produces(MediaType.APPLICATION_JSON)
    Response register(
            @PathParam("realmName") String realmName,
            @HeaderParam("Authorization") String token,
            UserRegistration user
    );

    @PUT
    @Path("/admin/realms/{realm}/users/{userId}/execute-actions-email")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendEmail(
            @HeaderParam("Authorization") String token,
            @PathParam("realm") String realm,
            @PathParam("userId") String userId,
            List<String> actions
    );

    @POST
    @Path("/realms/{realmName}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshToken(
            @PathParam("realmName") String realmName,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Path("/realms/{realmName}/protocol/openid-connect/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void logout(
            @PathParam("realmName") String realmName,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Path("/realms/{realmName}/protocol/openid-connect/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    IntrospectResponse introspectToken(
            @PathParam("realmName") String realmName,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("token") String token
    );
}
