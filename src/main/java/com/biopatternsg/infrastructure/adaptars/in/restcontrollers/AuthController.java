package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserAuthentication;
import com.biopatternsg.infrastructure.dtos.LoginRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.Response;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Path("/config-and-control/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthentication userAuthentication;

    @POST
    @Path("/login")
    @Operation(
        summary = "User authentication",
        description = "Authenticates a user with provided credentials and returns an authentication token."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User successfully authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = Response.class,
                    description = "Authentication token response"
                )
            )
        )
    })
    public jakarta.ws.rs.core.Response login(@Valid LoginRequest request) {

        Response token = userAuthentication.login(request.username(), request.password());
        return jakarta.ws.rs.core.Response.ok(token).build();
    }

}
