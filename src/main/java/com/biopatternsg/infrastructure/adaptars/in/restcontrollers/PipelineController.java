package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.CreatePipeline;
import com.biopatternsg.domain.port.in.LaunchPipeline;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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
    @Operation(
        summary = "Create pipeline configuration",
        description = "Creates a new pipeline configuration for biological data processing workflows."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Pipeline configuration successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PipelineConfig.class,
                    description = "Created pipeline configuration"
                )
            )
        )
    })
    public PipelineConfig create(PipelineConfig pipelineConfig){

        return createPipeline.execute(pipelineConfig);
    }

    @PUT
    @Operation(
        summary = "Update pipeline configuration",
        description = "Updates an existing pipeline configuration for biological data processing workflows."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Pipeline configuration successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PipelineConfig.class,
                    description = "Updated pipeline configuration"
                )
            )
        )
    })
    public PipelineConfig update(PipelineConfig pipelineConfig){

        return updatePipeline.execute(pipelineConfig);
    }


    @POST
    @Path("/launch")
    @Operation(
        summary = "Launch pipeline execution",
        description = "Launches the execution of a configured pipeline for biological data processing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "202",
            description = "Pipeline successfully launched for execution",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.STRING,
                    description = "Success message with pipeline ID"
                )
            )
        )
    })
    public Response launch(@QueryParam("id") String pipelineId){
        launchPipeline.execute(pipelineId);
        return Response.accepted().entity("Pipeline launched: " + pipelineId).build();
    }

    @PATCH
    @Path("/update-step/{id}")
    @Operation(
        summary = "Update pipeline step",
        description = "Updates a specific step within a pipeline configuration."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "202",
            description = "Pipeline step successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.STRING,
                    description = "Success message"
                )
            )
        )
    })
    public Response updateStep(@PathParam("id") String pipelineId, PipelineStepRequest stepRequest){
        updatePipelineStep.execute(pipelineId, stepRequest);

        return Response.accepted().entity("updated").build();
    }
}
