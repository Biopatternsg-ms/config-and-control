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

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.in.FindNetwork;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import com.biopatternsg.infrastructure.adaptars.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.dtos.NetworkResponse;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@Path("/config-and-control/networks")
@RequiredArgsConstructor
public class NetworkController {

    private final CreateNetwork createNetwork;
    private final UpdateNetwork updateNetwork;
    private final FindNetwork findNetwork;

    @POST
    @Operation(
        summary = "Create network configuration",
        description = "Creates a new network configuration for biological data processing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Network configuration successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = NetworkConfig.class,
                    description = "Created network configuration"
                )
            )
        )
    })
    public Response create(@Valid CreateNetworkRequest newNetwork){

        return Response.status(Response.Status.CREATED)
                .entity(createNetwork.execute(NetworkMapper.requestToModel(newNetwork)))
                .build();
    }

    @PUT
    @Operation(
        summary = "Update network configuration",
        description = "Updates an existing network configuration for biological data processing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Network configuration successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = NetworkConfig.class,
                    description = "Updated network configuration"
                )
            )
        )
    })
    public NetworkConfig update(@Valid UpdateNetworkRequest updateNetwork){

        return this.updateNetwork.execute(NetworkMapper.requestToModel(updateNetwork));
    }

    @GET
    @Operation(
            summary = "User network information",
            description = "Get network information for an Id network of user logged"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Network information successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = NetworkConfig.class,
                                    description = "Network information obtained"
                            )
                    )
            )
    })
    @Path("/{id}")
    public NetworkConfig findList(@PathParam("id") String id){

        return findNetwork.byId(id);
    }

    @GET
    @Operation(
            summary = "List user networks",
            description = "List networks associates for an user with option to apply same filters"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User networks list successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = NetworkConfig.class,
                                    description = "User networks obtained"
                            )
                    )
            )
    })
    public List<NetworkResponse> findList(FindNetworkRequest networkFilters){

        var NetworkConfigList = findNetwork.byFilters(NetworkMapper.requestToModel(networkFilters),
                networkFilters.page(), networkFilters.size());
        return NetworkMapper.toNetworkResponseList(NetworkConfigList);
    }
}
