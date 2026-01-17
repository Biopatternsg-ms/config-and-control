package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.infrastructure.clients.BiologicalObjectHttpClient;
import com.biopatternsg.infrastructure.dtos.LaunchPipelineRequest;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@RequiredArgsConstructor
public class BiologicalObjectAdapter implements BiologicalObjectRepository {

    @Inject
    private SessionUtil sessionUtil;
    @Inject
    @RestClient
    private BiologicalObjectHttpClient biologicalObjectHttpClient;

    @Override
    public String launch(PipelineConfig pipelineConfig) {

        var pipelineBiologicalObject = new LaunchPipelineRequest(
                pipelineConfig.getId(),
                pipelineConfig.getLevels(),
                pipelineConfig.getExpertObjects(),
                pipelineConfig.getTranscriptionFactorConfig());
        return biologicalObjectHttpClient.launch(pipelineBiologicalObject, sessionUtil.getUserId());
    }
}
