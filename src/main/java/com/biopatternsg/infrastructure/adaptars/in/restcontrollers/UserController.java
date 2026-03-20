package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Path("/config-and-control/user")
@RequiredArgsConstructor
public class UserController {

    private final UserManagement userManagement;

    @POST
    @Path("/register")
    public Response register(@Valid UserRequest request) {

        userManagement.register(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/recovery/{id}")
    public Response recover(@PathParam("id") String userId) {

        userManagement.recover(userId);
        return Response.accepted().build();
    }
}
