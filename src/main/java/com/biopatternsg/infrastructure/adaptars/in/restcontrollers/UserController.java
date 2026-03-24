package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.infrastructure.dtos.keycloak.UserRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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
    @Operation(
        summary = "Register new user",
        description = "Registers a new user in the system with the provided credentials and information."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.STRING,
                    description = "Registration confirmation"
                )
            )
        )
    })
    public Response register(@Valid UserRequest request) {

        userManagement.register(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/recovery/{id}")
    @Operation(
        summary = "Recover user account",
        description = "Initiates the account recovery process for a user with the specified ID."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "202",
            description = "Account recovery process initiated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.STRING,
                    description = "Recovery confirmation"
                )
            )
        )
    })
    public Response recover(@PathParam("id") String userId) {

        userManagement.recover(userId);
        return Response.accepted().build();
    }
}
