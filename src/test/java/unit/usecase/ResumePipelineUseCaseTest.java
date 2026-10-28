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
import com.biopatternsg.application.usecase.ResumePipelineUseCase;
import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineResumeSummary;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import com.biopatternsg.domain.port.out.repositories.NetworkRepository;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ResumePipelineUseCaseTest {

    static class FakePipelineRepository implements PipelineRepository {
        List<PipelineSteps> capturedSteps;
        List<PipelineConfig> inProgressPipelinesToReturn = new ArrayList<>();

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
        public List<PipelineConfig> findInProgressBySteps(List<PipelineSteps> steps) {
            this.capturedSteps = steps;
            return inProgressPipelinesToReturn;
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
        Map<String, String> capturedUserIds = new HashMap<>();
        Set<String> idsToFail = new HashSet<>();

        @Override
        public String launch(PipelineConfig pipelineConfig) {
            return launchConsole(pipelineConfig, null);
        }

        @Override
        public String launchConsole(PipelineConfig pipelineConfig, String userId) {
            if (idsToFail.contains(pipelineConfig.getId())) {
                throw new RuntimeException("Launch biological computation service failure");
            }
            launchedPipelines.add(pipelineConfig);
            capturedUserIds.put(pipelineConfig.getId(), userId);
            return "OK";
        }
    }

    static class FakePipelineStepOrchestrator extends PipelineStepOrchestrator {
        Map<String, PipelineSteps> orchestratedSteps = new HashMap<>();
        Set<String> idsToFail = new HashSet<>();

        FakePipelineStepOrchestrator() {
            super(null, null, null, null);
        }

        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            if (idsToFail.contains(pipelineConfig.getId())) {
                throw new RuntimeException("PubMed orchestrator step execution failure");
            }
            orchestratedSteps.put(pipelineConfig.getId(), step);
        }
    }

    private FakePipelineRepository pipelineRepository;
    private FakeNetworkRepository networkRepository;
    private FakeBiologicalObjectRepository biologicalObjectRepository;
    private FakePipelineStepOrchestrator pipelineStepOrchestrator;
    private ResumePipelineUseCase useCase;

    @BeforeEach
    void setUp() {
        pipelineRepository = new FakePipelineRepository();
        networkRepository = new FakeNetworkRepository();
        biologicalObjectRepository = new FakeBiologicalObjectRepository();
        pipelineStepOrchestrator = new FakePipelineStepOrchestrator();

        useCase = new ResumePipelineUseCase(
                pipelineRepository,
                networkRepository,
                biologicalObjectRepository,
                pipelineStepOrchestrator
        );
    }

    @Test
    @DisplayName("SEARCH_BIOLOGICAL_OBJECTS: should query biological steps 2..4 and launch computation")
    void execute_biologicalObjects_success() {
        NetworkConfig net = NetworkConfig.builder().id("net-1").userId("user-123").build();
        networkRepository.networks.put("net-1", net);

        PipelineConfig p1 = PipelineConfig.builder()
                .id("pipe-1")
                .networkId("net-1")
                .step(PipelineSteps.TRANSCRIPTION_FACTOR)
                .statuses(List.of(new PipelineStatus(PipelineSteps.TRANSCRIPTION_FACTOR, Status.IN_PROGRESS, new Date(), null)))
                .build();
        PipelineConfig p2 = PipelineConfig.builder()
                .id("pipe-2")
                .networkId("net-1")
                .step(PipelineSteps.SEARCH_LEVELS)
                .statuses(List.of(new PipelineStatus(PipelineSteps.SEARCH_LEVELS, Status.IN_PROGRESS, new Date(), null)))
                .build();

        pipelineRepository.inProgressPipelinesToReturn = List.of(p1, p2);

        PipelineResumeSummary summary = useCase.execute(MicroService.SEARCH_BIOLOGICAL_OBJECTS);

        assertNotNull(summary);
        assertEquals(MicroService.SEARCH_BIOLOGICAL_OBJECTS, summary.getMicroService());
        assertEquals(2, summary.getTotalFound());
        assertEquals(2, summary.getResumedCount());
        assertEquals(0, summary.getFailedCount());
        assertEquals(List.of("pipe-1", "pipe-2"), summary.getResumedPipelineIds());
        assertTrue(summary.getFailedPipelineIds().isEmpty());
        assertTrue(summary.getErrors().isEmpty());

        assertEquals(List.of(PipelineSteps.TRANSCRIPTION_FACTOR, PipelineSteps.EXPERT_OBJECTS, PipelineSteps.SEARCH_LEVELS),
                pipelineRepository.capturedSteps);
        assertEquals(2, biologicalObjectRepository.launchedPipelines.size());
        assertEquals("user-123", biologicalObjectRepository.capturedUserIds.get("pipe-1"));
        assertEquals("user-123", biologicalObjectRepository.capturedUserIds.get("pipe-2"));
    }

    @Test
    @DisplayName("SEARCH_BIOLOGICAL_OBJECTS: should handle zero candidates gracefully")
    void execute_biologicalObjects_noCandidates() {
        pipelineRepository.inProgressPipelinesToReturn = Collections.emptyList();

        PipelineResumeSummary summary = useCase.execute(MicroService.SEARCH_BIOLOGICAL_OBJECTS);

        assertEquals(0, summary.getTotalFound());
        assertEquals(0, summary.getResumedCount());
        assertEquals(0, summary.getFailedCount());
        assertTrue(summary.getResumedPipelineIds().isEmpty());
        assertTrue(summary.getFailedPipelineIds().isEmpty());
    }

    @Test
    @DisplayName("SEARCH_BIOLOGICAL_OBJECTS: should be resilient when a pipeline network or userId is missing")
    void execute_biologicalObjects_missingNetwork_resilientBatch() {
        NetworkConfig net = NetworkConfig.builder().id("net-ok").userId("user-abc").build();
        networkRepository.networks.put("net-ok", net);

        PipelineConfig pValid = PipelineConfig.builder()
                .id("pipe-valid")
                .networkId("net-ok")
                .step(PipelineSteps.EXPERT_OBJECTS)
                .build();
        PipelineConfig pInvalid = PipelineConfig.builder()
                .id("pipe-invalid")
                .networkId("net-missing")
                .step(PipelineSteps.EXPERT_OBJECTS)
                .build();

        pipelineRepository.inProgressPipelinesToReturn = List.of(pValid, pInvalid);

        PipelineResumeSummary summary = useCase.execute(MicroService.SEARCH_BIOLOGICAL_OBJECTS);

        assertEquals(2, summary.getTotalFound());
        assertEquals(1, summary.getResumedCount());
        assertEquals(1, summary.getFailedCount());
        assertEquals(List.of("pipe-valid"), summary.getResumedPipelineIds());
        assertEquals(List.of("pipe-invalid"), summary.getFailedPipelineIds());
        assertTrue(summary.getErrors().containsKey("pipe-invalid"));
        assertEquals(1, biologicalObjectRepository.launchedPipelines.size());
        assertEquals("user-abc", biologicalObjectRepository.capturedUserIds.get("pipe-valid"));
    }

    @Test
    @DisplayName("SEARCH_BIOLOGICAL_OBJECTS: should record failure when launch throws exception without aborting batch")
    void execute_biologicalObjects_launchThrows_resilientBatch() {
        NetworkConfig net = NetworkConfig.builder().id("net-1").userId("user-123").build();
        networkRepository.networks.put("net-1", net);

        PipelineConfig pFail = PipelineConfig.builder().id("pipe-fail").networkId("net-1").step(PipelineSteps.SEARCH_LEVELS).build();
        PipelineConfig pSuccess = PipelineConfig.builder().id("pipe-success").networkId("net-1").step(PipelineSteps.SEARCH_LEVELS).build();

        biologicalObjectRepository.idsToFail.add("pipe-fail");
        pipelineRepository.inProgressPipelinesToReturn = List.of(pFail, pSuccess);

        PipelineResumeSummary summary = useCase.execute(MicroService.SEARCH_BIOLOGICAL_OBJECTS);

        assertEquals(2, summary.getTotalFound());
        assertEquals(1, summary.getResumedCount());
        assertEquals(1, summary.getFailedCount());
        assertEquals(List.of("pipe-success"), summary.getResumedPipelineIds());
        assertEquals(List.of("pipe-fail"), summary.getFailedPipelineIds());
        assertTrue(summary.getErrors().get("pipe-fail").contains("Launch biological computation service failure"));
    }

    @Test
    @DisplayName("PUBMED_INTEGRATION: should map all steps 5..9 to their respective predecessors")
    void execute_pubmedIntegration_allStepsMapped() {
        NetworkConfig net = NetworkConfig.builder().id("net-pm").userId("pm-user-456").build();
        networkRepository.networks.put("net-pm", net);

        PipelineConfig p5 = PipelineConfig.builder().id("pipe-5").networkId("net-pm").step(PipelineSteps.COMBINATIONS).build();
        PipelineConfig p6 = PipelineConfig.builder().id("pipe-6").networkId("net-pm").step(PipelineSteps.SEARCH_PUBMED_IDS).build();
        PipelineConfig p7 = PipelineConfig.builder().id("pipe-7").networkId("net-pm").step(PipelineSteps.SEARCH_PUBTATOR).build();
        PipelineConfig p8 = PipelineConfig.builder().id("pipe-8").networkId("net-pm").step(PipelineSteps.BUILD_KNOWLEDGE_BASE).build();
        PipelineConfig p9 = PipelineConfig.builder().id("pipe-9").networkId("net-pm").step(PipelineSteps.GENERATE_ALIGNED_OBJECTS).build();

        pipelineRepository.inProgressPipelinesToReturn = List.of(p5, p6, p7, p8, p9);

        PipelineResumeSummary summary = useCase.execute(MicroService.PUBMED_INTEGRATION);

        assertEquals(MicroService.PUBMED_INTEGRATION, summary.getMicroService());
        assertEquals(5, summary.getTotalFound());
        assertEquals(5, summary.getResumedCount());
        assertEquals(0, summary.getFailedCount());

        assertEquals(List.of(
                PipelineSteps.COMBINATIONS,
                PipelineSteps.SEARCH_PUBMED_IDS,
                PipelineSteps.SEARCH_PUBTATOR,
                PipelineSteps.BUILD_KNOWLEDGE_BASE,
                PipelineSteps.GENERATE_ALIGNED_OBJECTS
        ), pipelineRepository.capturedSteps);

        assertEquals(PipelineSteps.SEARCH_LEVELS, pipelineStepOrchestrator.orchestratedSteps.get("pipe-5"));
        assertEquals(PipelineSteps.COMBINATIONS, pipelineStepOrchestrator.orchestratedSteps.get("pipe-6"));
        assertEquals(PipelineSteps.SEARCH_PUBMED_IDS, pipelineStepOrchestrator.orchestratedSteps.get("pipe-7"));
        assertEquals(PipelineSteps.SEARCH_PUBTATOR, pipelineStepOrchestrator.orchestratedSteps.get("pipe-8"));
        assertEquals(PipelineSteps.BUILD_KNOWLEDGE_BASE, pipelineStepOrchestrator.orchestratedSteps.get("pipe-9"));
    }

    @Test
    @DisplayName("PUBMED_INTEGRATION: should be resilient when orchestrate throws exception")
    void execute_pubmedIntegration_orchestrateThrows_resilientBatch() {
        NetworkConfig net = NetworkConfig.builder().id("net-pm").userId("user-pm").build();
        networkRepository.networks.put("net-pm", net);

        PipelineConfig pErr = PipelineConfig.builder().id("pipe-err").networkId("net-pm").step(PipelineSteps.COMBINATIONS).build();
        PipelineConfig pOk = PipelineConfig.builder().id("pipe-ok").networkId("net-pm").step(PipelineSteps.SEARCH_PUBMED_IDS).build();

        pipelineStepOrchestrator.idsToFail.add("pipe-err");
        pipelineRepository.inProgressPipelinesToReturn = List.of(pErr, pOk);

        PipelineResumeSummary summary = useCase.execute(MicroService.PUBMED_INTEGRATION);

        assertEquals(2, summary.getTotalFound());
        assertEquals(1, summary.getResumedCount());
        assertEquals(1, summary.getFailedCount());
        assertEquals(List.of("pipe-ok"), summary.getResumedPipelineIds());
        assertEquals(List.of("pipe-err"), summary.getFailedPipelineIds());
        assertTrue(summary.getErrors().get("pipe-err").contains("PubMed orchestrator step execution failure"));
    }
}
