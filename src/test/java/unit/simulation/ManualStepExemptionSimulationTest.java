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

class ManualStepExemptionSimulationTest {

    private FakePipelineRepository pipelineRepository;
    private FakeNetworkRepository networkRepository;
    private FakeBiologicalObjectRepository biologicalObjectRepository;
    private FakePipelineStepOrchestrator pipelineStepOrchestrator;
    private FakeUserSessionPort userSessionPort;
    private ResumeFailedPipelinesUseCase useCase;

    static class FakePipelineRepository implements PipelineRepository {
        List<PipelineConfig> storage = new ArrayList<>();

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            storage.add(pipelineConfig);
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) { return null; }

        @Override
        public PipelineConfig findByName(String name) { return null; }

        @Override
        public PipelineConfig findByNameExists(String networkId, String name) { return null; }

        @Override
        public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) { return null; }

        @Override
        public List<PipelineConfig> findAllFailed() { return storage; }
    }

    static class FakeNetworkRepository implements NetworkRepository {
        @Override
        public NetworkConfig save(NetworkConfig networkConfig) { return networkConfig; }

        @Override
        public NetworkConfig findById(String id) {
            return NetworkConfig.builder().id(id).userId("biologist-user").build();
        }

        @Override
        public NetworkConfig findByName(String name) { return null; }

        @Override
        public NetworkConfig findByNameExists(String id, String name) { return null; }

        @Override
        public ReportFormat<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size) { return null; }
    }

    static class FakeBiologicalObjectRepository implements BiologicalObjectRepository {
        int launchCalls = 0;
        @Override
        public String launch(PipelineConfig pipelineConfig) {
            launchCalls++;
            return "OK";
        }
    }

    static class FakePipelineStepOrchestrator extends PipelineStepOrchestrator {
        int orchestrateCalls = 0;
        public FakePipelineStepOrchestrator() { super(null, null, null, null); }
        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            orchestrateCalls++;
        }
    }

    static class FakeUserSessionPort implements UserSessionPort {
        @Override public void setUserId(String userId) {}
        @Override public String getUserId() { return null; }
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
    }

    @Test
    @DisplayName("Simulate deployment recovery when pipelines failed at manual review steps")
    void simulateRecovery_manualStepsExempted() {
        PipelineConfig manualAligned = PipelineConfig.builder()
                .id("pipe-manual-aligned")
                .networkId("net-1")
                .step(PipelineSteps.UPDATE_ALIGNED_OBJECTS)
                .statuses(List.of(
                        PipelineStatus.builder()
                                .step(PipelineSteps.UPDATE_ALIGNED_OBJECTS)
                                .status(Status.FAILED)
                                .createdAt(new Date())
                                .build()
                ))
                .build();

        PipelineConfig manualInferences = PipelineConfig.builder()
                .id("pipe-manual-inferences")
                .networkId("net-1")
                .step(PipelineSteps.CONFIGURE_INFERENCES)
                .statuses(List.of(
                        PipelineStatus.builder()
                                .step(PipelineSteps.CONFIGURE_INFERENCES)
                                .status(Status.FAILED)
                                .createdAt(new Date())
                                .build()
                ))
                .build();

        pipelineRepository.save(manualAligned);
        pipelineRepository.save(manualInferences);

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(2, response.getTotalFailed());
        assertEquals(0, response.getResumed());
        assertEquals(2, response.getSkipped());
        assertEquals(0, response.getErrors());

        assertEquals(0, biologicalObjectRepository.launchCalls);
        assertEquals(0, pipelineStepOrchestrator.orchestrateCalls);

        for (PipelineResumeDetail detail : response.getDetails()) {
            assertEquals("SKIPPED_MANUAL_STEP", detail.getResultStatus());
            assertNull(detail.getResumedFromStep());
            assertTrue(detail.getMessage().contains("waiting for manual user validation in UI"));
        }
    }
}
