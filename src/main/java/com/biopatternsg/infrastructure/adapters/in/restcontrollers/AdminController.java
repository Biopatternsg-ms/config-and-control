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
package com.biopatternsg.infrastructure.adapters.in.restcontrollers;

import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.infrastructure.adapters.mappers.UserMapper;
import com.biopatternsg.infrastructure.dtos.UserFiltersRequest;
import com.biopatternsg.infrastructure.dtos.keycloak.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;

@ApplicationScoped
@Path("/config-and-control/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagement userManagement;

    @GET
    @Path("/users")
    @Operation(
            summary = "List keycloak users",
            description = "List keycloak users with option to apply same filters"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Users list successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = UserResponse.class,
                                    description = "Users list obtained"
                            )
                    )
            )
    })
    public List<UserResponse> listUsers(UserFiltersRequest userFilters){

        var userList = userManagement.listUsers(UserMapper.filtersRequestToModel(userFilters));
        return UserMapper.modelToResponseList(userList);
    }
}
