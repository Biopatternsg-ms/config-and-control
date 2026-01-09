package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.domain.models.PipelineConfig;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ontologies-api")
public interface BiologicalObjectHttpClient {

    @POST
    @Path("/biological-object/launch-pipeline")
    void launch(@RequestBody PipelineConfig pipelineConfig);
}
