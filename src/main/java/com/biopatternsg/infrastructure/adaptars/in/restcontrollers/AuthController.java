package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserAuthentication;
import com.biopatternsg.infrastructure.dtos.LoginRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Path("/config-and-control/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthentication userAuthentication;

    @POST
    public jakarta.ws.rs.core.Response login(LoginRequest request) {

        Response token = userAuthentication.login(request.user(), request.pass());
        return jakarta.ws.rs.core.Response.ok(token).build();
    }

}
