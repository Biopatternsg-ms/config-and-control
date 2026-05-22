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
package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.UpdatePipelineRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineUseCase implements UpdatePipeline {

    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(UpdatePipelineRequest pipelineRequest) {

        var pipelineConfig = requestToConfig(pipelineRequest);
        //Pipeline don't exists
        var pipelineConfigCurrent = pipelineRepository.findById(pipelineConfig.getId());
        if(pipelineConfigCurrent == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }
        //Pipeline exists in network
        var findPipelineName = pipelineRepository.findByNameExists(pipelineConfigCurrent.getNetworkId(), pipelineConfig.getName());
        if(findPipelineName != null){
            throw new UnprocessableEntityException("The pipeline name already exists");
        }

        pipelineConfigCurrent.setName(pipelineConfig.getName());
        pipelineConfigCurrent.setDescription(pipelineConfig.getDescription());
        pipelineConfigCurrent.setLevels(pipelineConfig.getLevels());
        pipelineConfigCurrent.setExpertObjects(pipelineConfig.getExpertObjects());
        pipelineConfigCurrent.setTranscriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig());

        return pipelineRepository.save(pipelineConfigCurrent);
    }

    private PipelineConfig requestToConfig(UpdatePipelineRequest pipelineRequest){

        return PipelineConfig.builder()
                .id(pipelineRequest.id())
                .name(pipelineRequest.name())
                .description(pipelineRequest.description())
                .levels(pipelineRequest.levels())
                .expertObjects(pipelineRequest.expertObjects())
                .transcriptionFactorConfig(pipelineRequest.transcriptionFactorConfig())
                .build();
    }
}
