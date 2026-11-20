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
import com.biopatternsg.application.usecase.UpdatePipelineUseCase;
import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.enums.UpdatePipelineEnum;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePipelineUseCaseTest {

    static class FakePipelineRepository implements PipelineRepository {
        Map<String, PipelineConfig> storage = new HashMap<>();

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            storage.put(pipelineConfig.getId(), pipelineConfig);
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) {
            return storage.get(id);
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
            return Collections.emptyList();
        }
    }

    static class FakePipelineStepOrchestrator implements PipelineStepOrchestrator {
        AtomicBoolean orchestrateCalled = new AtomicBoolean(false);

        @Override
        public void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step) {
            orchestrateCalled.set(true);
        }

        @Override
        public void resumePipeline(PipelineConfig pipeline, String userId, MicroService microService) {
        }
    }

    private FakePipelineRepository repository;
    private FakePipelineStepOrchestrator orchestrator;
    private UpdatePipelineUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakePipelineRepository();
        orchestrator = new FakePipelineStepOrchestrator();
        useCase = new UpdatePipelineUseCase(repository, orchestrator);
    }

    @Test
    @DisplayName("Updating ALIGNED_EXPERT_OBJECTS confirms objects, delegates to domain, saves, and triggers orchestrator")
    void execute_alignedExpertObjects_delegatesAndOrchestrates() {
        PipelineConfig existing = PipelineConfig.builder()
                .id("pipe-1")
                .name("Pipeline One")
                .statuses(new ArrayList<>())
                .build();
        repository.storage.put("pipe-1", existing);

        PipelineConfig updateRequest = PipelineConfig.builder()
                .id("pipe-1")
                .alignedExpertObjects(List.of("TP53", "EGFR"))
                .build();

        PipelineConfig result = useCase.execute(updateRequest, UpdatePipelineEnum.ALIGNED_EXPERT_OBJECTS);

        assertNotNull(result);
        assertEquals(List.of("TP53", "EGFR"), result.getAlignedExpertObjects());
        assertEquals(PipelineSteps.UPDATE_ALIGNED_OBJECTS, result.getStep());
        assertTrue(orchestrator.orchestrateCalled.get(), "Orchestrator should be invoked for ALIGNED_EXPERT_OBJECTS");
    }

    @Test
    @DisplayName("Updating SEARCH_CONFIG completes step when all config fields are satisfied")
    void execute_searchConfig_completesStepWhenEligible() {
        PipelineConfig existing = PipelineConfig.builder()
                .id("pipe-2")
                .name("Pipeline Two")
                .description("Valid description")
                .statuses(new ArrayList<>())
                .build();
        repository.storage.put("pipe-2", existing);

        PipelineConfig updateRequest = PipelineConfig.builder()
                .id("pipe-2")
                .levels(2)
                .retMax(50)
                .maxComplexes(5)
                .useOnlyPrincipalName(true)
                .build();

        PipelineConfig result = useCase.execute(updateRequest, UpdatePipelineEnum.SEARCH_CONFIG);

        assertNotNull(result);
        assertEquals(2, result.getLevels());
        assertEquals(50, result.getRetMax());
        assertEquals(5, result.getMaxComplexes());
        assertTrue(result.isUseOnlyPrincipalName());

        assertTrue(result.getStatuses().stream()
                .anyMatch(ps -> ps.getStep() == PipelineSteps.CONFIG && ps.getStatus() == Status.COMPLETED));
    }

    @Test
    @DisplayName("Throws UnprocessableEntityException when pipeline does not exist")
    void execute_pipelineNotFound_throwsException() {
        PipelineConfig req = PipelineConfig.builder().id("non-existent").build();
        assertThrows(UnprocessableEntityException.class, () ->
                useCase.execute(req, UpdatePipelineEnum.DESCRIPTION)
        );
    }
}
