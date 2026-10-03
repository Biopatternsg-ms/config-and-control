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

import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.port.out.TriggerInferences;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.services.PipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PipelineStepOrchestratorTest {

    static class FakeTriggerInferences implements TriggerInferences {
        AtomicBoolean executeFindRolesCalled = new AtomicBoolean(false);
        AtomicReference<PipelineConfig> capturedConfig = new AtomicReference<>();

        @Override
        public void executeFindRoles(PipelineConfig pipelineConfig) {
            executeFindRolesCalled.set(true);
            capturedConfig.set(pipelineConfig);
        }
    }

    static class FakeTriggerPubmedIntegration implements TriggerPubmedIntegration {
        AtomicBoolean executeBuildsPairsCalled = new AtomicBoolean(false);
        AtomicBoolean executeSearchPubmedIdsCalled = new AtomicBoolean(false);
        AtomicBoolean executeSearchPubtatorCalled = new AtomicBoolean(false);
        AtomicBoolean executeBuildKnowledgeBaseCalled = new AtomicBoolean(false);
        AtomicBoolean executeGenerateAlignedObjectsCalled = new AtomicBoolean(false);

        @Override
        public void executeBuildsPairs(PipelineConfig pipelineConfig) {
            executeBuildsPairsCalled.set(true);
        }

        @Override
        public void executeSearchPubmedIds(PipelineConfig pipelineConfig) {
            executeSearchPubmedIdsCalled.set(true);
        }

        @Override
        public void executeSearchPubtator(PipelineConfig pipelineConfig) {
            executeSearchPubtatorCalled.set(true);
        }

        @Override
        public void executeBuildKnowledgeBase(PipelineConfig pipelineConfig) {
            executeBuildKnowledgeBaseCalled.set(true);
        }

        @Override
        public void executeGenerateAlignedObjects(PipelineConfig pipelineConfig) {
            executeGenerateAlignedObjectsCalled.set(true);
        }
    }

    static class StepRecord {
        final String id;
        final PipelineSteps step;
        final Status status;

        StepRecord(String id, PipelineSteps step, Status status) {
            this.id = id;
            this.step = step;
            this.status = status;
        }
    }

    static class FakePipelineService extends PipelineService {
        final List<StepRecord> stepRecords = new ArrayList<>();

        FakePipelineService() {
            super(null);
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
            stepRecords.add(new StepRecord(id, step, status));
            return PipelineConfig.builder().id(id).build();
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics) {
            stepRecords.add(new StepRecord(id, step, status));
            return PipelineConfig.builder().id(id).build();
        }
    }

    private FakeTriggerInferences triggerInferences;
    private FakeTriggerPubmedIntegration triggerPubmedIntegration;
    private FakePipelineService pipelineService;
    private PipelineStepOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        triggerInferences = new FakeTriggerInferences();
        triggerPubmedIntegration = new FakeTriggerPubmedIntegration();
        pipelineService = new FakePipelineService();

        orchestrator = new PipelineStepOrchestrator(
                triggerPubmedIntegration,
                triggerInferences,
                null,
                pipelineService
        );
    }

    @Test
    @DisplayName("When CONFIGURE_INFERENCES completes, orchestrator triggers executeFindRoles on TriggerInferences")
    void orchestrate_configureInferences_triggersFindRoles() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-100").build();

        orchestrator.orchestrate(config, PipelineSteps.CONFIGURE_INFERENCES);

        assertTrue(triggerInferences.executeFindRolesCalled.get(),
                "executeFindRoles must be invoked when CONFIGURE_INFERENCES completes");
        assertEquals("pipe-100", triggerInferences.capturedConfig.get().getId());
    }

    @Test
    @DisplayName("When FIND_ROLES completes, orchestrator handles it gracefully without exception")
    void orchestrate_findRoles_handledGracefully() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-101").build();

        assertDoesNotThrow(() -> orchestrator.orchestrate(config, PipelineSteps.FIND_ROLES));
        assertFalse(triggerInferences.executeFindRolesCalled.get());
    }

    @Test
    @DisplayName("When GENERATE_ALIGNED_OBJECTS completes, orchestrator sets UPDATE_ALIGNED_OBJECTS to IN_PROGRESS")
    void orchestrate_generateAlignedObjects_setsUpdateAlignedInProgress() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-102").build();

        orchestrator.orchestrate(config, PipelineSteps.GENERATE_ALIGNED_OBJECTS);

        assertEquals(1, pipelineService.stepRecords.size());
        StepRecord record = pipelineService.stepRecords.get(0);
        assertEquals("pipe-102", record.id);
        assertEquals(PipelineSteps.UPDATE_ALIGNED_OBJECTS, record.step);
        assertEquals(Status.IN_PROGRESS, record.status);
    }

    @Test
    @DisplayName("When UPDATE_ALIGNED_OBJECTS completes, orchestrator sets CONFIGURE_INFERENCES to IN_PROGRESS")
    void orchestrate_updateAlignedObjects_setsConfigureInferencesInProgress() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-103").build();

        orchestrator.orchestrate(config, PipelineSteps.UPDATE_ALIGNED_OBJECTS);

        assertEquals(1, pipelineService.stepRecords.size());
        StepRecord record = pipelineService.stepRecords.get(0);
        assertEquals("pipe-103", record.id);
        assertEquals(PipelineSteps.CONFIGURE_INFERENCES, record.step);
        assertEquals(Status.IN_PROGRESS, record.status);
    }
}
