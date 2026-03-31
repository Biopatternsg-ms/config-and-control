package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.*;
import com.biopatternsg.infrastructure.dtos.*;
import jakarta.validation.Valid;
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

import java.util.List;

@ApplicationScoped
@Path("/config-and-control/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final CreatePipeline createPipeline;
    private final LaunchPipeline launchPipeline;
    private final UpdatePipeline updatePipeline;
    private final FindPipeline findPipeline;
    private final UpdatePipelineStep updatePipelineStep;

    @POST
    @Operation(
        summary = "Create pipeline configuration",
        description = "Creates a new pipeline configuration for biological data processing workflows."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
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
    public Response create(@Valid CreatePipelineRequest newPipeline){

        return Response.status(Response.Status.CREATED)
                .entity(createPipeline.execute(newPipeline))
                .build();
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
    public PipelineConfig update(UpdatePipelineRequest pipelineRequest){

        return updatePipeline.execute(pipelineRequest);
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
    public Response launch(LaunchPipelineRequest pipelineRequest){
        launchPipeline.execute(pipelineRequest);
        return Response.accepted().entity("Pipeline launched: " + pipelineRequest).build();
    }

    @PATCH
    @Path("/update-step")
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
    public Response updateStep(@Valid PipelineStepRequest stepRequest){
        updatePipelineStep.execute(stepRequest);

        return Response.accepted().entity("updated").build();
    }

    @GET
    @Operation(
            summary = "User pipeline information",
            description = "Get pipeline information for an Id pipeline of user logged"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pipeline information successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = NetworkConfig.class,
                                    description = "Pipeline information obtained"
                            )
                    )
            )
    })
    @Path("/{id}")
    public PipelineConfig findList(@PathParam("id") String id){

        return findPipeline.byId(id);
    }

    @GET
    @Operation(
            summary = "List user pipelines",
            description = "List pipelines associates for an user with option to apply same filters"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User pipelines list successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = NetworkConfig.class,
                                    description = "User pipelines obtained"
                            )
                    )
            )
    })
    public List<PipelineConfig> findList(FindPipelineRequest findPipelineRequest){

        return findPipeline.byFilters(findPipelineRequest);
    }
}
