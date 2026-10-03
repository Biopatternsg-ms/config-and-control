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

import com.biopatternsg.application.usecase.GetPipelineExecutionUseCase;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.*;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GetPipelineExecutionUseCaseTest {

    static class FakePipelineRepository implements PipelineRepository {
        PipelineConfig configToReturn;

        @Override
        public PipelineConfig save(PipelineConfig pipelineConfig) {
            return pipelineConfig;
        }

        @Override
        public PipelineConfig findById(String id) {
            return configToReturn;
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
    }

    private FakePipelineRepository repository;
    private GetPipelineExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakePipelineRepository();
        useCase = new GetPipelineExecutionUseCase(repository);
    }

    @Test
    @DisplayName("execute should return step-find_roles with correct name, icon, description and PENDING when no status")
    void execute_findRolesPending() {
        repository.configToReturn = PipelineConfig.builder()
                .id("pipe-1")
                .name("Test Pipeline")
                .statuses(Collections.emptyList())
                .build();

        ExperimentExecutionResponse response = useCase.execute("pipe-1");

        assertNotNull(response);
        PipelineStepExecutionResponse findRolesStep = response.steps().stream()
                .filter(s -> "step-find_roles".equals(s.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(findRolesStep, "step-find_roles must be present in execution response");
        assertEquals("Find Biological Roles", findRolesStep.name());
        assertEquals("Activity", findRolesStep.iconName());
        assertEquals("Identifies biological roles and classifications for aligned entities.", findRolesStep.description());
        assertEquals("PENDING", findRolesStep.status());
    }

    @Test
    @DisplayName("execute should map FIND_ROLES IN_PROGRESS to ACTIVE status")
    void execute_findRolesInProgress_mapsToActive() {
        Date now = new Date();
        PipelineStatus status = PipelineStatus.builder()
                .step(PipelineSteps.FIND_ROLES)
                .status(Status.IN_PROGRESS)
                .createdAt(now)
                .build();

        repository.configToReturn = PipelineConfig.builder()
                .id("pipe-1")
                .name("Test Pipeline")
                .statuses(List.of(status))
                .build();

        ExperimentExecutionResponse response = useCase.execute("pipe-1");

        PipelineStepExecutionResponse findRolesStep = response.steps().stream()
                .filter(s -> "step-find_roles".equals(s.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(findRolesStep);
        assertEquals("ACTIVE", findRolesStep.status());
    }

    @Test
    @DisplayName("execute should map FIND_ROLES COMPLETED status and include execution metrics")
    void execute_findRolesCompleted_includesMetrics() {
        Date start = new Date(System.currentTimeMillis() - 10000);
        Date end = new Date();
        Map<String, String> metrics = Map.of(
                "meshIdsFound", "128",
                "rolesIdentified", "128",
                "entitiesWithActiveRoles", "115"
        );

        PipelineStatus inProgress = PipelineStatus.builder()
                .step(PipelineSteps.FIND_ROLES)
                .status(Status.IN_PROGRESS)
                .createdAt(start)
                .build();

        PipelineStatus completed = PipelineStatus.builder()
                .step(PipelineSteps.FIND_ROLES)
                .status(Status.COMPLETED)
                .metrics(metrics)
                .createdAt(end)
                .build();

        repository.configToReturn = PipelineConfig.builder()
                .id("pipe-1")
                .name("Test Pipeline")
                .statuses(List.of(inProgress, completed))
                .build();

        ExperimentExecutionResponse response = useCase.execute("pipe-1");

        PipelineStepExecutionResponse findRolesStep = response.steps().stream()
                .filter(s -> "step-find_roles".equals(s.id()))
                .findFirst()
                .orElse(null);

        assertNotNull(findRolesStep);
        assertEquals("COMPLETED", findRolesStep.status());
        assertNotNull(findRolesStep.metrics());
        assertEquals("128", findRolesStep.metrics().get("meshIdsFound"));
        assertEquals("115", findRolesStep.metrics().get("entitiesWithActiveRoles"));
    }
}
