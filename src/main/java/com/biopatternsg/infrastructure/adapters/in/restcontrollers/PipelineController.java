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

import com.biopatternsg.domain.enums.UpdatePipelineEnum;
import com.biopatternsg.domain.models.ExperimentExecutionResponse;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.in.*;
import com.biopatternsg.infrastructure.adapters.mappers.PipelineMapper;
import com.biopatternsg.infrastructure.dtos.*;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.domain.services.PipelineService;
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
    private final GetPipelineExecution getPipelineExecution;
    private final PipelineService pipelineService;
    private final TriggerPubmedIntegration triggerPubmedIntegration;

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
                .entity(createPipeline.execute(PipelineMapper.requestToCreate(newPipeline)))
                .build();
    }


    @PUT
    @Path("/description")
    @Operation(
        summary = "Update pipeline description",
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
                    implementation = UpdatePipelineDescriptionRequest.class,
                    description = "Updated pipeline configuration"
                )
            )
        )
    })
    public PipelineConfig updateDescription(@Valid UpdatePipelineDescriptionRequest updatePipeline){

        return this.updatePipeline.execute(PipelineMapper.requestToUpdate(updatePipeline),
                UpdatePipelineEnum.DESCRIPTION);
    }

    @PUT
    @Path("/transcription-factor")
    @Operation(
            summary = "Update pipeline transcription factor",
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
                                    implementation = UpdatePipelineTranscriptionFactorRequest.class,
                                    description = "Updated pipeline configuration"
                            )
                    )
            )
    })
    public PipelineConfig updateTranscriptionFactor(@Valid UpdatePipelineTranscriptionFactorRequest updatePipeline){

        return this.updatePipeline.execute(PipelineMapper.requestToUpdate(updatePipeline),
                UpdatePipelineEnum.TRANSCRIPTION_FACTOR);
    }

    @PUT
    @Path("/expert-objects")
    @Operation(
            summary = "Update pipeline expert objects",
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
                                    implementation = UpdatePipelineExpertObjectsRequest.class,
                                    description = "Updated pipeline configuration"
                            )
                    )
            )
    })
    public PipelineConfig updateExpertObjects(@Valid UpdatePipelineExpertObjectsRequest updatePipeline){

        return this.updatePipeline.execute(PipelineMapper.requestToUpdate(updatePipeline),
                UpdatePipelineEnum.EXPERT_OBJETS);
    }

    @PUT
    @Path("/search-config")
    @Operation(
            summary = "Update pipeline search configuration",
            description = "Updates the search level and maximum Pubtator results (retMax) of an existing pipeline."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pipeline search configuration successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = UpdatePipelineSearchConfigRequest.class,
                                    description = "Updated pipeline configuration"
                            )
                    )
            )
    })
    public PipelineConfig updateSearchConfig(@Valid UpdatePipelineSearchConfigRequest updatePipeline){

        return this.updatePipeline.execute(PipelineMapper.requestToUpdate(updatePipeline),
                UpdatePipelineEnum.SEARCH_CONFIG);
    }

    @PUT
    @Path("/aligned-expert-objects")
    @Operation(
            summary = "Update pipeline aligned expert objects",
            description = "Updates the list of confirmed aligned biological symbols (alignedExpertObjects) of an existing pipeline."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pipeline aligned expert objects successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = UpdateAlignedExpertObjectsRequest.class,
                                    description = "Updated pipeline configuration"
                            )
                    )
            )
    })
    public PipelineConfig updateAlignedExpertObjects(@Valid UpdateAlignedExpertObjectsRequest updatePipeline){

        return this.updatePipeline.execute(PipelineMapper.requestToUpdate(updatePipeline),
                UpdatePipelineEnum.ALIGNED_EXPERT_OBJECTS);
    }

    @POST
    @Path("/{id}/regenerate-aligned-objects")
    @Operation(
        summary = "Regenerate aligned objects",
        description = "Re-triggers the aligned objects generation step with updated symbols."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "202",
            description = "Regeneration process successfully initiated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = SchemaType.STRING,
                    description = "Success message"
                )
            )
        )
    })
    public Response regenerateAlignedObjects(@PathParam("id") String id){
        var pipeline = findPipeline.byId(id);
        if (pipeline == null) {
            throw new UnprocessableEntityException("The pipeline don't exists");
        }
        pipelineService.updateStep(id, PipelineSteps.UPDATE_ALIGNED_OBJECTS, Status.PENDING);
        triggerPubmedIntegration.executeGenerateAlignedObjects(pipeline);
        return Response.accepted().entity("Regeneration triggered").build();
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
    public Response launch(LaunchPipelineRequest launchPipelineRequest){
        launchPipeline.execute(launchPipelineRequest.pipelineId());
        return Response.accepted().entity("Pipeline launched").build();
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
        updatePipelineStep.execute(stepRequest.id(), PipelineMapper.requestToStatus(stepRequest));

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
                                    implementation = PipelineConfig.class,
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
                                    implementation = PipelineResponse.class,
                                    description = "User pipelines obtained"
                            )
                    )
            )
    })
    public ReportFormat<PipelineResponse> findList(FindPipelineRequest pipelineFilters){

        var modelFormat = findPipeline.byFilters(PipelineMapper.requestToUpdate(pipelineFilters),
                pipelineFilters.page(), pipelineFilters.size());
        var responseList = modelFormat.list().stream().map(PipelineMapper::configToResponse).toList();
        return new ReportFormat<>(modelFormat.count(), responseList);
    }

    @GET
    @Path("/{id}/execution")
    @Operation(
            summary = "Get pipeline execution progress",
            description = "Retrieves current execution status, timeline duration, and step statuses for a pipeline."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pipeline execution progress successfully obtained",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = ExperimentExecutionResponse.class,
                                    description = "Pipeline execution status and steps"
                            )
                    )
            )
    })
    public ExperimentExecutionResponse getExecutionProgress(@PathParam("id") String id){
        return getPipelineExecution.execute(id);
    }
}
