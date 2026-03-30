package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.domain.models.UserRegister;
import com.biopatternsg.infrastructure.dtos.UsersKeycloakFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.IntrospectResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.LoginClientResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;
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
    LoginClientResponse loginClient(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret
    );

    @POST
    @Path("/realms/biopatternsg/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response loginUser(
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
    Response refreshToken(
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
            UserRegister userRegister
    );

    @GET
    @Path("admin/realms/biopatternsg/users")
    List<UserResponse> usersList(
            @HeaderParam("Authorization") String token,
            @BeanParam UsersKeycloakFiltersRequest usersKeycloakFilters
    );

    @PUT
    @Path("/admin/realms/biopatternsg/users/{userId}/execute-actions-email")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendEmail(
            @HeaderParam("Authorization") String token,
            @PathParam("userId") String userId,
            List<String> actions
    );
}
