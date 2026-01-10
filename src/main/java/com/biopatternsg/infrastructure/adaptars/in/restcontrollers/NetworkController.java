package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@ApplicationScoped
@Path("/networks")
@RequiredArgsConstructor
public class NetworkController {

    private final CreateNetwork createNetwork;
    private final UpdateNetwork updateNetwork;

    @POST
    public NetworkConfig create(@RequestBody NetworkConfig networkConfig){

        return createNetwork.execute(networkConfig);
    }

    @PUT
    public NetworkConfig update(@RequestBody NetworkConfig networkConfig){

        return updateNetwork.execute(networkConfig);
    }
}
