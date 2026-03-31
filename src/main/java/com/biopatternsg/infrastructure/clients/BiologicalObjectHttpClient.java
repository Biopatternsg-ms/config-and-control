package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.infrastructure.dtos.LaunchPipelineInternalRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "biological-object-api")
public interface BiologicalObjectHttpClient {

    @POST
    @Path("/biological-object/launch-pipeline")
    String launch(@RequestBody LaunchPipelineInternalRequest pipelineRequest, @HeaderParam("x-user-id") String userId);
}
