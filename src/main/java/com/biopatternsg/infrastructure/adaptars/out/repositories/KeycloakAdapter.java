package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.exceptions.KeycloakServiceException;
import com.biopatternsg.domain.port.out.repositories.KeycloakRepository;
import com.biopatternsg.infrastructure.clients.KeycloakHttpClient;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRegistration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class KeycloakAdapter implements KeycloakRepository {

    @Inject
    @RestClient
    private KeycloakHttpClient keycloakHttpClient;

    @ConfigProperty(name = "keycloak.client.id")
    String clientId;
    @ConfigProperty(name = "keycloak.client.secret")
    String clientSecret;
    @ConfigProperty(name = "keycloak.scope")
    String scope;
    @ConfigProperty(name = "keycloak.realm-name")
    String realmName;
    @ConfigProperty(name = "keycloak.grant-type")
    String grantType;
    @ConfigProperty(name = "keycloak.grant-type-client")
    String grantTypeClient;

    public Response login(String user, String pass) {

        try{
            return keycloakHttpClient.loginUser(realmName, grantType, clientId, clientSecret, user, pass, scope);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    public Response register(UserRegistration newUser) {

        var credentials = keycloakHttpClient.loginClient(realmName, grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.access_token();

        try{
            return keycloakHttpClient.register(realmName, accessToken, newUser);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    public void verifyEmail(String userId){

        var credentials = keycloakHttpClient.loginClient(realmName, grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.access_token();
        List<String> actions = List.of("VERIFY_EMAIL", "UPDATE_PASSWORD");

        try{
            keycloakHttpClient.sendEmail(realmName, accessToken, userId, actions);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

    public void recoveryPassword(String userId) {

        var credentials = keycloakHttpClient.loginClient(realmName, grantTypeClient, clientId, clientSecret);
        var accessToken = "Bearer " + credentials.access_token();
        List<String> actions = List.of("UPDATE_PASSWORD");

        try{
            keycloakHttpClient.sendEmail(realmName, accessToken, userId, actions);
        } catch (WebApplicationException e) {
            throw new KeycloakServiceException(e.getResponse().getStatus());
        }
    }

}
