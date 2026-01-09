package com.biopatternsg.infrastructure.adaptars.in.restcontrollers;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.CreatePipeline;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@ApplicationScoped
@Path("/control")
@RequiredArgsConstructor
public class ControlController {

    final CreatePipeline createPipeline;

    @POST
    @Path("/pipeline")
    public void execute(@RequestBody PipelineConfig PipelineConfig){

        createPipeline.execute(PipelineConfig);
    }
}
