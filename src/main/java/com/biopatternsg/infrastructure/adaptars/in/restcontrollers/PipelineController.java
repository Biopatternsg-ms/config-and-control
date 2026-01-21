package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.CreatePipeline;
import com.biopatternsg.domain.port.in.LaunchPipeline;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Path("/config-and-control/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final CreatePipeline createPipeline;
    private final LaunchPipeline launchPipeline;
    private final UpdatePipeline updatePipeline;

    @POST
    public PipelineConfig create(PipelineConfig pipelineConfig){

        return createPipeline.execute(pipelineConfig);
    }

    @PUT
    public PipelineConfig update(PipelineConfig pipelineConfig){

        return updatePipeline.execute(pipelineConfig);
    }


    @POST
    @Path("/launch")
    public Response launch(@QueryParam("id") String pipelineId){

        var response = launchPipeline.execute(pipelineId);
        return Response.accepted().entity("Biological object result: " + response).build();
    }
}
