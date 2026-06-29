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
package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.infrastructure.adaptars.mappers.UserMapper;
import com.biopatternsg.infrastructure.dtos.UserRequest;
import jakarta.ws.rs.GET;
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
@Path("/config-and-control/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagement userManagement;

    @POST
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
    public Response register(@Valid UserRequest newUser) {

        userManagement.register(UserMapper.userRequestToModel(newUser));
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}/password-recovery")
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
    public Response passwordRecover(@PathParam("id") String userId) {

        userManagement.recover(userId);
        return Response.accepted().build();
    }
}
