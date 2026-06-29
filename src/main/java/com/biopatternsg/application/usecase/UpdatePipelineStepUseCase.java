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

import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.services.PipelineService;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.port.in.UpdatePipelineStep;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UpdatePipelineStepUseCase implements UpdatePipelineStep {

    private final PipelineService pipelineService;
    private final PipelineStepOrchestrator pipelineStepOrchestrator;

    @Override
    public void execute(String pipelineId, PipelineStatus pipelineStatus) {

        var pipelineConfig = pipelineService.updateStep(
                pipelineId, pipelineStatus.getStep(), pipelineStatus.getStatus());

        if (pipelineStatus.getStatus() == Status.COMPLETED) {
            pipelineStepOrchestrator.orchestrate(pipelineConfig, pipelineStatus.getStep());
        }

        log.info("Pipeline {}, step {} updated",pipelineId, pipelineStatus.getStep().getValue());
    }
}
