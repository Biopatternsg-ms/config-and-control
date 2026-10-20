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
package unit.simulation;

import com.biopatternsg.application.services.PipelineStepOrchestrator;
import com.biopatternsg.application.usecase.ResumeFailedPipelinesUseCase;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.*;
import com.biopatternsg.domain.port.out.UserSessionPort;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentRecoverySimulationTest {

    private FakePipelineRepository pipelineRepository;
    private FakeNetworkRepository networkRepository;
    private TrackingBiologicalObjectRepository biologicalObjectRepository;
    private TrackingPipelineStepOrchestrator pipelineStepOrchestrator;
    private FakeUserSessionPort userSessionPort;
    private ResumeFailedPipelinesUseCase useCase;

    static class FakePipelineRepository implements PipelineRepository {
        Map<String, PipelineConfig> storage = new LinkedHashMap<>();

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            storage.put(pipelineConfig.getId(), pipelineConfig);
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) { return storage.get(id); }

        @Override
        public PipelineConfig findByName(String name) { return null; }

        @Override
        public PipelineConfig findByNameExists(String networkId, String name) { return null; }

        @Override
        public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) { return null; }

        @Override
        public List<PipelineConfig> findAllFailed() {
            return new ArrayList<>(storage.values());
        }
    }

    static class FakeNetworkRepository implements NetworkRepository {
        Map<String, NetworkConfig> networks = new HashMap<>();

        @Override
        public NetworkConfig save(NetworkConfig networkConfig) { return networkConfig; }

        @Override
        public NetworkConfig findById(String id) { return networks.get(id); }

        @Override
        public NetworkConfig findByName(String name) { return null; }

        @Override
        public NetworkConfig findByNameExists(String id, String name) { return null; }

        @Override
        public ReportFormat<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size) { return null; }
    }

    static class TrackingBiologicalObjectRepository implements BiologicalObjectRepository {
        List<String> launchedPipelineIds = new ArrayList<>();

        @Override
        public String launch(PipelineConfig pipelineConfig) {
            launchedPipelineIds.add(pipelineConfig.getId());
            return "SUCCESS";
        }
    }

    static class TrackingPipelineStepOrchestrator extends PipelineStepOrchestrator {
        Map<String, PipelineSteps> dispatchedPredecessors = new LinkedHashMap<>();

        public TrackingPipelineStepOrchestrator() {
            super(null, null, null, null);
        }

        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            dispatchedPredecessors.put(pipelineConfig.getId(), step);
        }
    }

    static class FakeUserSessionPort implements UserSessionPort {
        String currentUserId;
        @Override
        public void setUserId(String userId) { this.currentUserId = userId; }
        @Override
        public String getUserId() { return currentUserId; }
    }

    @BeforeEach
    void setUp() {
        pipelineRepository = new FakePipelineRepository();
        networkRepository = new FakeNetworkRepository();
        biologicalObjectRepository = new TrackingBiologicalObjectRepository();
        pipelineStepOrchestrator = new TrackingPipelineStepOrchestrator();
        userSessionPort = new FakeUserSessionPort();

        useCase = new ResumeFailedPipelinesUseCase(
                pipelineRepository,
                networkRepository,
                biologicalObjectRepository,
                pipelineStepOrchestrator,
                userSessionPort
        );

        networkRepository.networks.put("net-sim", NetworkConfig.builder()
                .id("net-sim")
                .userId("researcher-001")
                .name("Simulation Network")
                .build());
    }

    private PipelineConfig createFailedPipeline(String id, PipelineSteps failedStep) {
        return PipelineConfig.builder()
                .id(id)
                .networkId("net-sim")
                .step(failedStep)
                .statuses(new ArrayList<>(List.of(
                        PipelineStatus.builder()
                                .step(failedStep)
                                .status(Status.FAILED)
                                .createdAt(new Date())
                                .metrics(Map.of("reason", "Simulated deployment interruption at " + failedStep))
                                .build()
                )))
                .build();
    }

    @Test
    @DisplayName("Simulate deployment recovery across all 7 automated steps")
    void simulateRecovery_acrossAll7AutomatedSteps() {
        // Step 1: SEARCH_LEVELS (or initial LAUNCH)
        pipelineRepository.save(createFailedPipeline("pipe-search-levels", PipelineSteps.SEARCH_LEVELS));
        // Step 2: COMBINATIONS
        pipelineRepository.save(createFailedPipeline("pipe-combinations", PipelineSteps.COMBINATIONS));
        // Step 3: SEARCH_PUBMED_IDS
        pipelineRepository.save(createFailedPipeline("pipe-pubmed-ids", PipelineSteps.SEARCH_PUBMED_IDS));
        // Step 4: SEARCH_PUBTATOR
        pipelineRepository.save(createFailedPipeline("pipe-pubtator", PipelineSteps.SEARCH_PUBTATOR));
        // Step 5: BUILD_KNOWLEDGE_BASE
        pipelineRepository.save(createFailedPipeline("pipe-kb", PipelineSteps.BUILD_KNOWLEDGE_BASE));
        // Step 6: GENERATE_ALIGNED_OBJECTS
        pipelineRepository.save(createFailedPipeline("pipe-aligned", PipelineSteps.GENERATE_ALIGNED_OBJECTS));
        // Step 7: FIND_ROLES
        pipelineRepository.save(createFailedPipeline("pipe-roles", PipelineSteps.FIND_ROLES));

        // When deployment triggers resumption
        ResumeFailedPipelinesResponse response = useCase.execute();

        // Then assert all 7 automated pipelines are resumed
        assertEquals(7, response.getTotalFailed());
        assertEquals(7, response.getResumed());
        assertEquals(0, response.getSkipped());
        assertEquals(0, response.getErrors());

        // Verify initial step re-launched via BiologicalObjectRepository
        assertTrue(biologicalObjectRepository.launchedPipelineIds.contains("pipe-search-levels"));

        // Verify predecessor step dispatched for each automated step
        assertEquals(PipelineSteps.SEARCH_LEVELS, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-combinations"));
        assertEquals(PipelineSteps.COMBINATIONS, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-pubmed-ids"));
        assertEquals(PipelineSteps.SEARCH_PUBMED_IDS, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-pubtator"));
        assertEquals(PipelineSteps.SEARCH_PUBTATOR, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-kb"));
        assertEquals(PipelineSteps.BUILD_KNOWLEDGE_BASE, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-aligned"));
        assertEquals(PipelineSteps.CONFIGURE_INFERENCES, pipelineStepOrchestrator.dispatchedPredecessors.get("pipe-roles"));
    }
}
