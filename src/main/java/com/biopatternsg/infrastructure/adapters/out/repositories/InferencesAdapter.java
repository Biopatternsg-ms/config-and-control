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

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.port.out.TriggerInferences;
import com.biopatternsg.domain.services.PipelineService;
import com.biopatternsg.infrastructure.clients.InferencesRestClient;
import com.biopatternsg.infrastructure.dtos.FindRolesRequest;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@ApplicationScoped
public class InferencesAdapter implements TriggerInferences {

    private final InferencesRestClient inferencesRestClient;
    private final PipelineService pipelineService;
    private final SessionUtil sessionUtil;

    @Inject
    public InferencesAdapter(@RestClient InferencesRestClient inferencesRestClient,
                             PipelineService pipelineService,
                             SessionUtil sessionUtil) {
        this.inferencesRestClient = inferencesRestClient;
        this.pipelineService = pipelineService;
        this.sessionUtil = sessionUtil;
    }

    @Override
    public void executeFindRoles(PipelineConfig pipelineConfig) {
        String userId = null;
        try {
            if (sessionUtil != null && sessionUtil.getContext() != null && sessionUtil.getContext().containsKey("x-user-id")) {
                userId = sessionUtil.getUserId();
            }
        } catch (Exception e) {
            log.warn("Could not retrieve userId from sessionUtil for pipeline {}: {}", pipelineConfig.getId(), e.getMessage());
        }

        if (userId == null || userId.isBlank()) {
            pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.FIND_ROLES, Status.FAILED);
            log.error("Cannot trigger FIND_ROLES for pipeline {}: userId is missing or empty in session context", pipelineConfig.getId());
            return;
        }

        List<String> alignedObjects = pipelineConfig.getAlignedExpertObjects();
        if (alignedObjects == null || alignedObjects.isEmpty()) {
            if (pipelineConfig.getExpertObjects() != null) {
                alignedObjects = pipelineConfig.getExpertObjects().stream()
                        .map(ExpertObjectConfig::getSymbol)
                        .filter(Objects::nonNull)
                        .toList();
            } else {
                alignedObjects = Collections.emptyList();
            }
        }

        log.info("Triggering FIND_ROLES for pipeline {} with {} aligned objects: {}",
                pipelineConfig.getId(), alignedObjects.size(), alignedObjects);

        FindRolesRequest request = new FindRolesRequest(
                pipelineConfig.getId(),
                alignedObjects
        );

        try {
            pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.FIND_ROLES, Status.IN_PROGRESS);
            inferencesRestClient.findRoles(request, userId);
            log.info("Inferences API findRoles called successfully for pipeline {}", pipelineConfig.getId());
        } catch (Exception e) {
            pipelineService.updateStep(pipelineConfig.getId(), PipelineSteps.FIND_ROLES, Status.FAILED);
            log.error("Error calling Inferences API findRoles for pipeline {}", pipelineConfig.getId(), e);
        }
    }
}
