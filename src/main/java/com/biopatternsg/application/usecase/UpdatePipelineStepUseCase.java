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
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.dtos.PipelineStepRequest;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineStepUseCase implements UpdatePipelineStep {

    private final PipelineRepository pipelineRepository;

    @Override
    public void execute(PipelineStepRequest stepRequest) {

        var pipelineConfig = pipelineRepository.findById(stepRequest.id());
        if(pipelineConfig == null){
            throw new UnprocessableEntityException("The pipeline don't exists");
        }

        pipelineConfig.setStep(stepRequest.step());
        pipelineRepository.save(pipelineConfig);
        log.info("Pipeline {}, step {} updated",stepRequest.id(), stepRequest.step().getValue());
    }
}
