package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@ApplicationScoped
@Path("/config-and-control/networks")
@RequiredArgsConstructor
public class NetworkController {

    private final CreateNetwork createNetwork;
    private final UpdateNetwork updateNetwork;

    @POST
    @Operation(
        summary = "Create network configuration",
        description = "Creates a new network configuration for biological data processing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
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
    public NetworkConfig create(@RequestBody NetworkConfig networkConfig){

        return createNetwork.execute(networkConfig);
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
    public NetworkConfig update(@RequestBody NetworkConfig networkConfig){

        return updateNetwork.execute(networkConfig);
    }
}
