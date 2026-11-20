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

import com.biopatternsg.application.services.impl.PipelineServiceImpl;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.exceptions.UnprocessableEntityException;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PipelineServiceTest {

    static class FakePipelineRepository implements PipelineRepository {
        PipelineConfig storedConfig;

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            this.storedConfig = pipelineConfig;
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) {
            if (storedConfig != null && storedConfig.getId().equals(id)) {
                return storedConfig;
            }
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
            return Collections.emptyList();
        }
    }

    private FakePipelineRepository repository;
    private PipelineServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = new FakePipelineRepository();
        service = new PipelineServiceImpl(repository);
    }

    @Test
    @DisplayName("updateStep with PipelineStatus should update step, status, metrics and save")
    void updateStep_withPipelineStatus_updatesAndSaves() {
        repository.storedConfig = PipelineConfig.builder()
                .id("pipe-1")
                .statuses(new ArrayList<>())
                .build();

        Map<String, String> metrics = Map.of("processedRecords", "50");
        PipelineStatus statusUpdate = PipelineStatus.builder()
                .step(PipelineSteps.FIND_ROLES)
                .status(Status.COMPLETED)
                .metrics(metrics)
                .build();

        PipelineConfig result = service.updateStep("pipe-1", statusUpdate);

        assertNotNull(result);
        assertEquals(PipelineSteps.FIND_ROLES, result.getStep());
        assertEquals(1, result.getStatuses().size());
        assertEquals(Status.COMPLETED, result.getStatuses().get(0).getStatus());
        assertEquals("50", result.getStatuses().get(0).getMetrics().get("processedRecords"));
    }

    @Test
    @DisplayName("updateStep with step and status (3 params) should wrap in PipelineStatus and update")
    void updateStep_withStepAndStatus_updates() {
        repository.storedConfig = PipelineConfig.builder()
                .id("pipe-1")
                .statuses(new ArrayList<>())
                .build();

        PipelineConfig result = service.updateStep("pipe-1", PipelineSteps.INFER_PATHWAYS, Status.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(PipelineSteps.INFER_PATHWAYS, result.getStep());
        assertEquals(1, result.getStatuses().size());
        assertEquals(Status.IN_PROGRESS, result.getStatuses().get(0).getStatus());
    }

    @Test
    @DisplayName("updateStep when pipeline does not exist should throw UnprocessableEntityException")
    void updateStep_pipelineNotFound_throwsException() {
        PipelineStatus statusUpdate = PipelineStatus.builder()
                .step(PipelineSteps.CONFIG)
                .status(Status.PENDING)
                .build();

        assertThrows(UnprocessableEntityException.class, () ->
                service.updateStep("non-existent", statusUpdate)
        );
    }
}
