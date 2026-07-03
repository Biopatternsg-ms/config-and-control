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

import com.biopatternsg.domain.enums.UpdatePipelineEnum;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineUseCase implements UpdatePipeline {

    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(PipelineConfig pipelineConfig, UpdatePipelineEnum pipelineEnum) {

        //Pipeline don't exists
        var pipelineCurrent = pipelineRepository.findById(pipelineConfig.getId());
        if(pipelineCurrent == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }
        //Pipeline exists in network
        var findPipelineName = pipelineRepository.findByNameExists(pipelineCurrent.getNetworkId(), pipelineCurrent.getName());
        if(findPipelineName != null && !Objects.equals(findPipelineName.getId(), pipelineCurrent.getId())){
            throw new UnprocessableEntityException("The pipeline name already exists");
        }

        switch (pipelineEnum){
            case DESCRIPTION -> updateDescription(pipelineConfig, pipelineCurrent);
            case TRANSCRIPTION_FACTOR -> updateTranscriptionFactor(pipelineConfig, pipelineCurrent);
            case EXPERT_OBJETS -> updateExportObjects(pipelineConfig, pipelineCurrent);
            case SEARCH_CONFIG -> updateSearchConfig(pipelineConfig, pipelineCurrent);
        }

        return pipelineRepository.save(pipelineCurrent);
    }

    private void updateDescription(PipelineConfig pipelineRequest, PipelineConfig pipelineCurrent){
        pipelineCurrent.setDescription(pipelineRequest.getDescription());
    }

    private void updateTranscriptionFactor(PipelineConfig pipelineRequest, PipelineConfig pipelineCurrent){
        pipelineCurrent.setTranscriptionFactorConfig(pipelineRequest.getTranscriptionFactorConfig());
    }

    private void updateExportObjects(PipelineConfig pipelineRequest, PipelineConfig pipelineCurrent){
        pipelineCurrent.setExpertObjects(pipelineRequest.getExpertObjects());
    }

    private void updateSearchConfig(PipelineConfig pipelineRequest, PipelineConfig pipelineCurrent){
        pipelineCurrent.setLevels(pipelineRequest.getLevels());
        pipelineCurrent.setRetMax(pipelineRequest.getRetMax());
    }
}
