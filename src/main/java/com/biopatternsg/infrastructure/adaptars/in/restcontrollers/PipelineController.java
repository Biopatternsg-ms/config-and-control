package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.CreatePipeline;
import com.biopatternsg.domain.port.in.LaunchPipeline;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;
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
    private final UpdatePipelineStep updatePipelineStep;

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
        launchPipeline.execute(pipelineId);
        return Response.accepted().entity("Pipeline launched: " + pipelineId).build();
    }

    @PATCH
    @Path("/update-step/{id}")
    public Response updateStep(@PathParam("id") String pipelineId, PipelineStepRequest stepRequest){
        updatePipelineStep.execute(pipelineId, stepRequest.step());

        return Response.accepted().entity("updated").build();
    }
}
