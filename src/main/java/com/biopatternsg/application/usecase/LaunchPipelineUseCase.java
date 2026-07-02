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

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.port.in.LaunchPipeline;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class LaunchPipelineUseCase implements LaunchPipeline {

    private final BiologicalObjectRepository biologicalObjectRepository;
    private final PipelineRepository pipelineRepository;

    @Override
    public void execute(String pipelineId) {

        var pipeline = pipelineRepository.findById(pipelineId);
        if(pipeline == null){
            throw new UnprocessableEntityException("The pipeline don't exist");
        }

        biologicalObjectRepository.launch(pipeline);

        pipeline.setStep(PipelineSteps.LAUNCH);
        pipelineRepository.save(pipeline);
    }
}
