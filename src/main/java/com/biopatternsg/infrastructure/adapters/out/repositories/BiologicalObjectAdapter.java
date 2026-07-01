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
package com.biopatternsg.infrastructure.adapters.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.infrastructure.clients.BiologicalObjectHttpClient;
import com.biopatternsg.infrastructure.dtos.LaunchPipelineInternalRequest;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.faulttolerance.Retry;
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
    @Retry
    public String launch(PipelineConfig pipelineConfig) {

        var pipelineBiologicalObject = new LaunchPipelineInternalRequest(
                pipelineConfig.getId(),
                pipelineConfig.getLevels(),
                pipelineConfig.getExpertObjects(),
                pipelineConfig.getTranscriptionFactorConfig());
        return biologicalObjectHttpClient.launch(pipelineBiologicalObject, sessionUtil.getUserId());
    }
}
