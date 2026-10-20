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
package unit.usecase;

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

class ResumeFailedPipelinesUseCaseTest {

    private FakePipelineRepository pipelineRepository;
    private FakeNetworkRepository networkRepository;
    private FakeBiologicalObjectRepository biologicalObjectRepository;
    private FakePipelineStepOrchestrator pipelineStepOrchestrator;
    private FakeUserSessionPort userSessionPort;
    private ResumeFailedPipelinesUseCase useCase;

    static class FakePipelineRepository implements PipelineRepository {
        List<PipelineConfig> failedPipelines = new ArrayList<>();

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) {
            return null;
        }

        @Override
        public PipelineConfig findByName(String name) {
            return null;
        }

        @Override
        public PipelineConfig findByNameExists(String networkId, String name) {
            return null;
        }

        @Override
        public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) {
            return null;
        }

        @Override
        public List<PipelineConfig> findAllFailed() {
            return failedPipelines;
        }
    }

    static class FakeNetworkRepository implements NetworkRepository {
        Map<String, NetworkConfig> networks = new HashMap<>();

        @Override
        public NetworkConfig save(NetworkConfig networkConfig) {
            return networkConfig;
        }

        @Override
        public NetworkConfig findById(String id) {
            return networks.get(id);
        }

        @Override
        public NetworkConfig findByName(String name) {
            return null;
        }

        @Override
        public NetworkConfig findByNameExists(String id, String name) {
            return null;
        }

        @Override
        public ReportFormat<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size) {
            return null;
        }
    }

    static class FakeBiologicalObjectRepository implements BiologicalObjectRepository {
        List<PipelineConfig> launchedPipelines = new ArrayList<>();
        boolean shouldThrow = false;

        @Override
        public String launch(PipelineConfig pipelineConfig) {
            if (shouldThrow) {
                throw new RuntimeException("Biological object service unreachable");
            }
            launchedPipelines.add(pipelineConfig);
            return "OK";
        }
    }

    static class FakePipelineStepOrchestrator extends PipelineStepOrchestrator {
        List<Map.Entry<PipelineConfig, PipelineSteps>> orchestrations = new ArrayList<>();
        boolean shouldThrow = false;

        public FakePipelineStepOrchestrator() {
            super(null, null, null, null);
        }

        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            if (shouldThrow) {
                throw new RuntimeException("Worker service error");
            }
            orchestrations.add(Map.entry(pipelineConfig, step));
        }
    }

    static class FakeUserSessionPort implements UserSessionPort {
        String lastUserId;

        @Override
        public void setUserId(String userId) {
            this.lastUserId = userId;
        }

        @Override
        public String getUserId() {
            return lastUserId;
        }
    }

    @BeforeEach
    void setUp() {
        pipelineRepository = new FakePipelineRepository();
        networkRepository = new FakeNetworkRepository();
        biologicalObjectRepository = new FakeBiologicalObjectRepository();
        pipelineStepOrchestrator = new FakePipelineStepOrchestrator();
        userSessionPort = new FakeUserSessionPort();

        useCase = new ResumeFailedPipelinesUseCase(
                pipelineRepository,
                networkRepository,
                biologicalObjectRepository,
                pipelineStepOrchestrator,
                userSessionPort
        );

        networkRepository.networks.put("net-1", NetworkConfig.builder()
                .id("net-1")
                .userId("user-123")
                .name("Network 1")
                .build());
    }

    @Test
    @DisplayName("Returns zero counts when no failed pipelines exist")
    void execute_noFailedPipelines() {
        ResumeFailedPipelinesResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(0, response.getTotalFailed());
        assertEquals(0, response.getResumed());
        assertEquals(0, response.getSkipped());
        assertEquals(0, response.getErrors());
        assertTrue(response.getDetails().isEmpty());
    }

    @Test
    @DisplayName("Resumes pipeline failed at initial step LAUNCH by calling biologicalObjectRepository.launch")
    void execute_resumesInitialStep() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-1")
                .networkId("net-1")
                .step(PipelineSteps.LAUNCH)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.LAUNCH).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
        pipelineRepository.failedPipelines.add(pipeline);

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(1, response.getTotalFailed());
        assertEquals(1, response.getResumed());
        assertEquals(0, response.getSkipped());
        assertEquals(0, response.getErrors());
        assertEquals(1, biologicalObjectRepository.launchedPipelines.size());
        assertEquals("user-123", userSessionPort.lastUserId);
    }

    @Test
    @DisplayName("Resumes pipeline failed at SEARCH_PUBMED_IDS with predecessor COMBINATIONS")
    void execute_resumesIntermediateStep() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-2")
                .networkId("net-1")
                .step(PipelineSteps.SEARCH_PUBMED_IDS)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.COMBINATIONS).status(Status.COMPLETED).createdAt(new Date()).build(),
                        PipelineStatus.builder().step(PipelineSteps.SEARCH_PUBMED_IDS).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
        pipelineRepository.failedPipelines.add(pipeline);

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(1, response.getTotalFailed());
        assertEquals(1, response.getResumed());
        assertEquals(1, pipelineStepOrchestrator.orchestrations.size());
        assertEquals(PipelineSteps.COMBINATIONS, pipelineStepOrchestrator.orchestrations.get(0).getValue());
        assertEquals("pipe-2", pipelineStepOrchestrator.orchestrations.get(0).getKey().getId());
    }

    @Test
    @DisplayName("Resumes pipeline failed at FIND_ROLES with predecessor CONFIGURE_INFERENCES")
    void execute_resumesFindRoles() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-find-roles")
                .networkId("net-1")
                .step(PipelineSteps.FIND_ROLES)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.CONFIGURE_INFERENCES).status(Status.COMPLETED).createdAt(new Date()).build(),
                        PipelineStatus.builder().step(PipelineSteps.FIND_ROLES).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
        pipelineRepository.failedPipelines.add(pipeline);

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(1, response.getTotalFailed());
        assertEquals(1, response.getResumed());
        assertEquals(PipelineSteps.CONFIGURE_INFERENCES, pipelineStepOrchestrator.orchestrations.get(0).getValue());
    }

    @Test
    @DisplayName("Excludes manual review steps from auto-dispatch and records SKIPPED_MANUAL_STEP")
    void execute_skipsManualSteps() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-manual")
                .networkId("net-1")
                .step(PipelineSteps.UPDATE_ALIGNED_OBJECTS)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.UPDATE_ALIGNED_OBJECTS).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
        pipelineRepository.failedPipelines.add(pipeline);

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(1, response.getTotalFailed());
        assertEquals(0, response.getResumed());
        assertEquals(1, response.getSkipped());
        assertEquals(0, response.getErrors());
        assertEquals("SKIPPED_MANUAL_STEP", response.getDetails().get(0).getResultStatus());
        assertTrue(pipelineStepOrchestrator.orchestrations.isEmpty());
        assertTrue(biologicalObjectRepository.launchedPipelines.isEmpty());
    }

    @Test
    @DisplayName("Isolates error when one pipeline throws exception and continues with remaining pipelines")
    void execute_errorIsolation() {
        PipelineConfig faultyPipeline = PipelineConfig.builder()
                .id("pipe-faulty")
                .networkId("net-1")
                .step(PipelineSteps.LAUNCH)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.LAUNCH).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
        PipelineConfig validPipeline = PipelineConfig.builder()
                .id("pipe-valid")
                .networkId("net-1")
                .step(PipelineSteps.COMBINATIONS)
                .statuses(List.of(
                        PipelineStatus.builder().step(PipelineSteps.COMBINATIONS).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();

        pipelineRepository.failedPipelines.add(faultyPipeline);
        pipelineRepository.failedPipelines.add(validPipeline);

        // Make biological launch throw
        biologicalObjectRepository.shouldThrow = true;

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(2, response.getTotalFailed());
        assertEquals(1, response.getResumed());
        assertEquals(1, response.getErrors());
        assertEquals("ERROR", response.getDetails().get(0).getResultStatus());
        assertEquals("RESUMED", response.getDetails().get(1).getResultStatus());
    }
}
