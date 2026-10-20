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

class BatchIsolationSimulationTest {

    private FakePipelineRepository pipelineRepository;
    private FakeNetworkRepository networkRepository;
    private FakeBiologicalObjectRepository biologicalObjectRepository;
    private SelectiveFailingOrchestrator pipelineStepOrchestrator;
    private FakeUserSessionPort userSessionPort;
    private ResumeFailedPipelinesUseCase useCase;

    static class FakePipelineRepository implements PipelineRepository {
        List<PipelineConfig> storage = new ArrayList<>();

        @Override public PipelineConfig save(PipelineConfig pipelineConfig) { return pipelineConfig; }
        @Override public PipelineConfig findById(String id) { return null; }
        @Override public PipelineConfig findByName(String name) { return null; }
        @Override public PipelineConfig findByNameExists(String networkId, String name) { return null; }
        @Override public ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) { return null; }
        @Override public List<PipelineConfig> findAllFailed() { return storage; }
    }

    static class FakeNetworkRepository implements NetworkRepository {
        Map<String, NetworkConfig> networks = new HashMap<>();

        @Override public NetworkConfig save(NetworkConfig networkConfig) { return networkConfig; }
        @Override public NetworkConfig findById(String id) { return networks.get(id); }
        @Override public NetworkConfig findByName(String name) { return null; }
        @Override public NetworkConfig findByNameExists(String id, String name) { return null; }
        @Override public ReportFormat<NetworkConfig> findByFilters(NetworkConfig findNetwork, int page, int size) { return null; }
    }

    static class FakeBiologicalObjectRepository implements BiologicalObjectRepository {
        @Override public String launch(PipelineConfig pipelineConfig) { return "OK"; }
    }

    static class SelectiveFailingOrchestrator extends PipelineStepOrchestrator {
        Set<String> failingPipelineIds = new HashSet<>();
        List<String> successfullyDispatched = new ArrayList<>();

        public SelectiveFailingOrchestrator() { super(null, null, null, null); }

        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            if (failingPipelineIds.contains(pipelineConfig.getId())) {
                throw new RuntimeException("Simulated HTTP 500 from worker for pipeline " + pipelineConfig.getId());
            }
            successfullyDispatched.add(pipelineConfig.getId());
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
        pipelineStepOrchestrator = new SelectiveFailingOrchestrator();
        userSessionPort = new FakeUserSessionPort();

        useCase = new ResumeFailedPipelinesUseCase(
                pipelineRepository,
                networkRepository,
                biologicalObjectRepository,
                pipelineStepOrchestrator,
                userSessionPort
        );

        networkRepository.networks.put("net-tenant-1", NetworkConfig.builder().id("net-tenant-1").userId("user-1").build());
        networkRepository.networks.put("net-tenant-2", NetworkConfig.builder().id("net-tenant-2").userId("user-2").build());
        networkRepository.networks.put("net-tenant-3", NetworkConfig.builder().id("net-tenant-3").userId("user-3").build());
    }

    private PipelineConfig createPipeline(String id, String networkId, PipelineSteps step) {
        return PipelineConfig.builder()
                .id(id)
                .networkId(networkId)
                .step(step)
                .statuses(List.of(
                        PipelineStatus.builder().step(step).status(Status.FAILED).createdAt(new Date()).build()
                ))
                .build();
    }

    @Test
    @DisplayName("Simulate batch resumption with error isolation: middle pipeline failure does not prevent subsequent resumptions")
    void simulateBatchResumption_isolatedErrors() {
        PipelineConfig pipe1 = createPipeline("pipe-1", "net-tenant-1", PipelineSteps.COMBINATIONS);
        PipelineConfig pipe2 = createPipeline("pipe-2-fails", "net-tenant-2", PipelineSteps.SEARCH_PUBMED_IDS);
        PipelineConfig pipe3 = createPipeline("pipe-3", "net-tenant-3", PipelineSteps.SEARCH_PUBTATOR);

        pipelineRepository.storage.add(pipe1);
        pipelineRepository.storage.add(pipe2);
        pipelineRepository.storage.add(pipe3);

        // Configure pipe2 to fail
        pipelineStepOrchestrator.failingPipelineIds.add("pipe-2-fails");

        ResumeFailedPipelinesResponse response = useCase.execute();

        assertEquals(3, response.getTotalFailed());
        assertEquals(2, response.getResumed());
        assertEquals(0, response.getSkipped());
        assertEquals(1, response.getErrors());

        assertEquals("RESUMED", response.getDetails().get(0).getResultStatus());
        assertEquals("pipe-1", response.getDetails().get(0).getPipelineId());

        assertEquals("ERROR", response.getDetails().get(1).getResultStatus());
        assertEquals("pipe-2-fails", response.getDetails().get(1).getPipelineId());
        assertTrue(response.getDetails().get(1).getMessage().contains("Simulated HTTP 500"));

        assertEquals("RESUMED", response.getDetails().get(2).getResultStatus());
        assertEquals("pipe-3", response.getDetails().get(2).getPipelineId());

        assertTrue(pipelineStepOrchestrator.successfullyDispatched.contains("pipe-1"));
        assertTrue(pipelineStepOrchestrator.successfullyDispatched.contains("pipe-3"));
    }
}
