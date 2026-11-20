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
import com.biopatternsg.application.services.impl.PipelineStepOrchestratorImpl;
import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.port.out.TriggerInferences;
import com.biopatternsg.domain.port.out.TriggerPubmedIntegration;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.application.services.PipelineService;
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
        AtomicBoolean executeInferPathwaysCalled = new AtomicBoolean(false);
        AtomicReference<PipelineConfig> capturedConfig = new AtomicReference<>();

        @Override
        public void executeFindRoles(PipelineConfig pipelineConfig) {
            executeFindRolesCalled.set(true);
            capturedConfig.set(pipelineConfig);
        }

        @Override
        public void executeInferPathways(PipelineConfig pipelineConfig) {
            executeInferPathwaysCalled.set(true);
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

    static class FakeBiologicalObjectRepository implements BiologicalObjectRepository {
        AtomicBoolean launchConsoleCalled = new AtomicBoolean(false);
        AtomicReference<String> capturedUserId = new AtomicReference<>();

        @Override
        public String launch(PipelineConfig pipelineConfig) {
            return null;
        }

        @Override
        public String launchConsole(PipelineConfig pipelineConfig, String userId) {
            launchConsoleCalled.set(true);
            capturedUserId.set(userId);
            return "OK";
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

    static class FakePipelineService implements PipelineService {
        final List<StepRecord> stepRecords = new ArrayList<>();

        

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
            stepRecords.add(new StepRecord(id, step, status));
            return PipelineConfig.builder().id(id).build();
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineStatus statusUpdate) {
            stepRecords.add(new StepRecord(id, statusUpdate.getStep(), statusUpdate.getStatus()));
            return PipelineConfig.builder().id(id).build();
        }
    }

    private FakeTriggerInferences triggerInferences;
    private FakeTriggerPubmedIntegration triggerPubmedIntegration;
    private FakeBiologicalObjectRepository biologicalObjectRepository;
    private FakePipelineService pipelineService;
    private PipelineStepOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        triggerInferences = new FakeTriggerInferences();
        triggerPubmedIntegration = new FakeTriggerPubmedIntegration();
        biologicalObjectRepository = new FakeBiologicalObjectRepository();
        pipelineService = new FakePipelineService();

        orchestrator = new PipelineStepOrchestratorImpl(
                triggerPubmedIntegration,
                triggerInferences,
                biologicalObjectRepository,
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
    @DisplayName("When FIND_ROLES completes, orchestrator sets UPDATE_BIOLOGICAL_OBJECTS to IN_PROGRESS")
    void orchestrate_findRoles_setsUpdateBiologicalObjectsInProgress() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-101").build();

        orchestrator.orchestrate(config, PipelineSteps.FIND_ROLES);

        assertEquals(1, pipelineService.stepRecords.size());
        StepRecord record = pipelineService.stepRecords.get(0);
        assertEquals("pipe-101", record.id);
        assertEquals(PipelineSteps.UPDATE_BIOLOGICAL_OBJECTS, record.step);
        assertEquals(Status.IN_PROGRESS, record.status);
    }

    @Test
    @DisplayName("When UPDATE_BIOLOGICAL_OBJECTS completes, orchestrator sets SELECT_CLOSING_OBJECTS to IN_PROGRESS")
    void orchestrate_updateBiologicalObjects_setsSelectClosingObjectsInProgress() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-101-bio").build();

        orchestrator.orchestrate(config, PipelineSteps.UPDATE_BIOLOGICAL_OBJECTS);

        assertEquals(1, pipelineService.stepRecords.size());
        StepRecord record = pipelineService.stepRecords.get(0);
        assertEquals("pipe-101-bio", record.id);
        assertEquals(PipelineSteps.SELECT_CLOSING_OBJECTS, record.step);
        assertEquals(Status.IN_PROGRESS, record.status);
    }

    @Test
    @DisplayName("When SELECT_CLOSING_OBJECTS completes, orchestrator handles it gracefully")
    void orchestrate_selectClosingObjects_handledGracefully() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-101-closing").build();

        assertDoesNotThrow(() -> orchestrator.orchestrate(config, PipelineSteps.SELECT_CLOSING_OBJECTS));
    }

    @Test
    @DisplayName("When INFER_PATHWAYS completes, orchestrator handles it gracefully")
    void orchestrate_inferPathways_handledGracefully() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-101-infer").build();

        assertDoesNotThrow(() -> orchestrator.orchestrate(config, PipelineSteps.INFER_PATHWAYS));
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

    @Test
    @DisplayName("resumePipeline with SEARCH_BIOLOGICAL_OBJECTS invokes biologicalObjectRepository.launchConsole")
    void resumePipeline_biologicalObjects_invokesLaunchConsole() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-bio").step(PipelineSteps.TRANSCRIPTION_FACTOR).build();

        orchestrator.resumePipeline(config, "user-42", MicroService.SEARCH_BIOLOGICAL_OBJECTS);

        assertTrue(biologicalObjectRepository.launchConsoleCalled.get());
        assertEquals("user-42", biologicalObjectRepository.capturedUserId.get());
    }

    @Test
    @DisplayName("resumePipeline with PUBMED_INTEGRATION resolves predecessor step and orchestrates")
    void resumePipeline_pubmedIntegration_resolvesPredecessorAndOrchestrates() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-pm").step(PipelineSteps.SEARCH_PUBMED_IDS).build();

        orchestrator.resumePipeline(config, "user-42", MicroService.PUBMED_INTEGRATION);

        assertTrue(triggerPubmedIntegration.executeSearchPubmedIdsCalled.get());
    }

    @Test
    @DisplayName("resumePipeline with unknown predecessor step throws IllegalStateException")
    void resumePipeline_unknownPredecessor_throwsException() {
        PipelineConfig config = PipelineConfig.builder().id("pipe-err").step(PipelineSteps.CONFIG).build();

        assertThrows(IllegalStateException.class, () ->
                orchestrator.resumePipeline(config, "user-42", MicroService.PUBMED_INTEGRATION)
        );
    }
}
