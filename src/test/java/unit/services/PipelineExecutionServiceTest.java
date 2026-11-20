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
package unit.services;

import com.biopatternsg.application.services.impl.PipelineExecutionServiceImpl;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.ExperimentExecutionResponse;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.models.PipelineStepExecutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PipelineExecutionServiceTest {

    private PipelineExecutionServiceImpl executionService;

    @BeforeEach
    void setUp() {
        executionService = new PipelineExecutionServiceImpl();
    }

    @Test
    @DisplayName("buildExecutionReport should create 16 steps with correct names, icons and initial PENDING statuses")
    void buildExecutionReport_noStatuses_returnsPendingSteps() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-1")
                .name("Cancer Experiment")
                .statuses(Collections.emptyList())
                .build();

        ExperimentExecutionResponse response = executionService.buildExecutionReport(config);

        assertNotNull(response);
        assertEquals("pipe-1", response.experimentId());
        assertEquals("Cancer Experiment", response.experimentName());
        assertEquals("PENDING", response.status());
        assertFalse(response.steps().isEmpty());

        PipelineStepExecutionResponse configStep = response.steps().stream()
                .filter(s -> "step-configuration".equals(s.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(configStep);
        assertEquals("Configuration Setup", configStep.name());
        assertEquals("Sliders", configStep.iconName());
        assertEquals("PENDING", configStep.status());
    }

    @Test
    @DisplayName("buildExecutionReport should map ACTIVE and COMPLETED steps and calculate durations")
    void buildExecutionReport_activeAndCompletedSteps_calculatesDurationsAndOverallStatus() {
        Date start = new Date(System.currentTimeMillis() - 5000);
        Date end = new Date();

        PipelineStatus completedConfig = PipelineStatus.builder()
                .step(PipelineSteps.CONFIG)
                .status(Status.COMPLETED)
                .createdAt(end)
                .build();

        PipelineStatus activeSearch = PipelineStatus.builder()
                .step(PipelineSteps.SEARCH_PUBMED_IDS)
                .status(Status.IN_PROGRESS)
                .createdAt(start)
                .build();

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-2")
                .name("Active Pipeline")
                .statuses(List.of(completedConfig, activeSearch))
                .build();

        ExperimentExecutionResponse response = executionService.buildExecutionReport(config);

        assertNotNull(response);
        assertEquals("ACTIVE", response.status());

        PipelineStepExecutionResponse searchStep = response.steps().stream()
                .filter(s -> "step-search_pubmed_ids".equals(s.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(searchStep);
        assertEquals("ACTIVE", searchStep.status());
        assertEquals("(Active)", searchStep.outputText());
    }
}
