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
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PipelineStepOrchestrator {

    private final TriggerPubmedIntegration triggerPubmedIntegration;

    public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
        switch (step) {
            case SEARCH_LEVELS -> {
                log.info("Step is SEARCH_LEVELS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeBuildsPairs(pipelineConfig);
            }
            case COMBINATIONS -> {
                log.info("Step is COMBINATIONS, triggering PubMed integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeSearchPubmedIds(pipelineConfig);
            }
            case SEARCH_PUBMED_IDS -> {
                log.info("Step is SEARCH_PUBMED_IDS, triggering PubTator integration for pipeline {}", pipelineConfig.getId());
                triggerPubmedIntegration.executeSearchPubtator(pipelineConfig);
            }
            default -> {
                log.info("Step is unknown, doing nothing");
            }
        }
    }
}
