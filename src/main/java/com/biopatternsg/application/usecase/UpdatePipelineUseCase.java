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

@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineUseCase implements UpdatePipeline {

    private final PipelineRepository pipelineRepository;

    @Override
    public PipelineConfig execute(PipelineConfig pipelineConfig, UpdatePipelineEnum pipelineEnum) {

        //Pipeline don't exists
        var pipelineConfigCurrent = pipelineRepository.findById(pipelineConfig.getId());
        if(pipelineConfigCurrent == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }
        //Pipeline exists in network
        var findPipelineName = pipelineRepository.findByNameExists(pipelineConfigCurrent.getNetworkId(), pipelineConfig.getName());
        if(findPipelineName != null && pipelineConfig.getName() != null){
            throw new UnprocessableEntityException("The pipeline name already exists");
        }

        switch (pipelineEnum){
            case INIT -> updateDescription(pipelineConfig, pipelineConfigCurrent);
            case TRANSCRIPTION_FACTOR -> updateTranscriptionFactor(pipelineConfig, pipelineConfigCurrent);
            case EXPERT_OBJETS -> updateExportObjects(pipelineConfig, pipelineConfigCurrent);
        }
        if (pipelineConfig.getName() != null)
            pipelineConfigCurrent.setName(pipelineConfig.getName());
        if (pipelineConfig.getDescription() != null)
            pipelineConfigCurrent.setDescription(pipelineConfig.getDescription());
        if (pipelineConfig.getLevels() != null)
            pipelineConfigCurrent.setLevels(pipelineConfig.getLevels());
        if (pipelineConfig.getExpertObjects() != null)
            pipelineConfigCurrent.setExpertObjects(pipelineConfig.getExpertObjects());
        if (pipelineConfig.getTranscriptionFactorConfig() != null)
            pipelineConfigCurrent.setTranscriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig());

        return pipelineRepository.save(pipelineConfigCurrent);
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
}
