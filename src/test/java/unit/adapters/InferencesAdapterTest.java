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
package unit.adapters;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.services.PipelineService;
import com.biopatternsg.infrastructure.adapters.out.repositories.InferencesAdapter;
import com.biopatternsg.infrastructure.clients.InferencesRestClient;
import com.biopatternsg.infrastructure.dtos.FindRolesRequest;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InferencesAdapterTest {

    static class FakeInferencesRestClient implements InferencesRestClient {
        AtomicBoolean findRolesCalled = new AtomicBoolean(false);
        AtomicReference<FindRolesRequest> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUserId = new AtomicReference<>();
        boolean shouldThrow = false;
        AtomicInteger callOrder = new AtomicInteger(0);
        int executionOrder = -1;

        @Override
        public void findRoles(FindRolesRequest request, String userId) {
            findRolesCalled.set(true);
            capturedRequest.set(request);
            capturedUserId.set(userId);
            executionOrder = callOrder.incrementAndGet();
            if (shouldThrow) {
                throw new RuntimeException("Inferences microservice error");
            }
        }
    }

    static class StepUpdateRecord {
        final String pipelineId;
        final PipelineSteps step;
        final Status status;
        final int order;

        StepUpdateRecord(String pipelineId, PipelineSteps step, Status status, int order) {
            this.pipelineId = pipelineId;
            this.step = step;
            this.status = status;
            this.order = order;
        }
    }

    static class FakePipelineService extends PipelineService {
        final List<StepUpdateRecord> updates = new ArrayList<>();
        final AtomicInteger callOrder;

        FakePipelineService(AtomicInteger callOrder) {
            super(null);
            this.callOrder = callOrder;
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
            updates.add(new StepUpdateRecord(id, step, status, callOrder.incrementAndGet()));
            return PipelineConfig.builder().id(id).build();
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics) {
            updates.add(new StepUpdateRecord(id, step, status, callOrder.incrementAndGet()));
            return PipelineConfig.builder().id(id).build();
        }
    }

    private FakeInferencesRestClient restClient;
    private FakePipelineService pipelineService;
    private SessionUtil sessionUtil;
    private InferencesAdapter adapter;
    private AtomicInteger globalOrder;

    @BeforeEach
    void setUp() {
        globalOrder = new AtomicInteger(0);
        restClient = new FakeInferencesRestClient();
        restClient.callOrder = globalOrder;
        pipelineService = new FakePipelineService(globalOrder);
        sessionUtil = new SessionUtil();

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("x-user-id", "test-user-123");
        sessionUtil.setContext(headers);

        adapter = new InferencesAdapter(restClient, pipelineService, sessionUtil);
    }

    @Test
    @DisplayName("executeFindRoles should mark IN_PROGRESS BEFORE calling Inferences REST client to avoid race conditions")
    void executeFindRoles_marksInProgressBeforeCallingRestClient() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-1")
                .alignedExpertObjects(List.of("CYP7A1", "FXR"))
                .build();

        adapter.executeFindRoles(config);

        // Verify REST client was called
        assertTrue(restClient.findRolesCalled.get());
        assertEquals("pipe-1", restClient.capturedRequest.get().pipelineId());
        assertEquals(List.of("CYP7A1", "FXR"), restClient.capturedRequest.get().alignedObjects());
        assertEquals("test-user-123", restClient.capturedUserId.get());

        // Verify status updates: must have exactly 1 update to IN_PROGRESS
        assertEquals(1, pipelineService.updates.size());
        StepUpdateRecord record = pipelineService.updates.get(0);
        assertEquals("pipe-1", record.pipelineId);
        assertEquals(PipelineSteps.FIND_ROLES, record.step);
        assertEquals(Status.IN_PROGRESS, record.status);

        // Crucial: verify IN_PROGRESS was recorded BEFORE the REST call executed
        assertTrue(record.order < restClient.executionOrder,
                "updateStep(IN_PROGRESS) must be executed before restClient.findRoles");
    }

    @Test
    @DisplayName("executeFindRoles should mark FAILED when Inferences REST client throws an exception")
    void executeFindRoles_restClientThrows_marksFailed() {
        restClient.shouldThrow = true;
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-error")
                .alignedExpertObjects(List.of("TP53"))
                .build();

        adapter.executeFindRoles(config);

        // Must record IN_PROGRESS first, then FAILED
        assertEquals(2, pipelineService.updates.size());
        assertEquals(Status.IN_PROGRESS, pipelineService.updates.get(0).status);
        assertEquals(Status.FAILED, pipelineService.updates.get(1).status);
        assertEquals(PipelineSteps.FIND_ROLES, pipelineService.updates.get(1).step);
        assertEquals("pipe-error", pipelineService.updates.get(1).pipelineId);
    }

    @Test
    @DisplayName("executeFindRoles should mark FAILED and not invoke REST client when userId is missing")
    void executeFindRoles_missingUserId_marksFailedAndAborts() {
        // Clear session context
        sessionUtil.setContext(new MultivaluedHashMap<>());

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-no-user")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        adapter.executeFindRoles(config);

        // REST client must NOT be called
        assertFalse(restClient.findRolesCalled.get(), "REST client must not be invoked when userId is missing");

        // Status must be marked as FAILED immediately
        assertEquals(1, pipelineService.updates.size());
        StepUpdateRecord record = pipelineService.updates.get(0);
        assertEquals("pipe-no-user", record.pipelineId);
        assertEquals(PipelineSteps.FIND_ROLES, record.step);
        assertEquals(Status.FAILED, record.status);
    }

    @Test
    @DisplayName("executeFindRoles should mark FAILED and not invoke REST client when session context is null")
    void executeFindRoles_nullSessionContext_marksFailedAndAborts() {
        sessionUtil.setContext(null);

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-null-ctx")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        adapter.executeFindRoles(config);

        assertFalse(restClient.findRolesCalled.get());
        assertEquals(1, pipelineService.updates.size());
        assertEquals(Status.FAILED, pipelineService.updates.get(0).status);
    }

    @Test
    @DisplayName("executeFindRoles should mark FAILED and not invoke REST client when userId is blank")
    void executeFindRoles_blankUserId_marksFailedAndAborts() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("x-user-id", "   ");
        sessionUtil.setContext(headers);

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-blank-user")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        adapter.executeFindRoles(config);

        assertFalse(restClient.findRolesCalled.get());
        assertEquals(1, pipelineService.updates.size());
        assertEquals(Status.FAILED, pipelineService.updates.get(0).status);
    }

    @Test
    @DisplayName("executeFindRoles should fallback to expertObjects when alignedExpertObjects is null or empty")
    void executeFindRoles_fallbackToExpertObjectsWhenAlignedEmpty() {
        ExpertObjectConfig eo1 = new ExpertObjectConfig();
        eo1.setSymbol("TP53");
        ExpertObjectConfig eo2 = new ExpertObjectConfig();
        eo2.setSymbol(null);
        ExpertObjectConfig eo3 = new ExpertObjectConfig();
        eo3.setSymbol("MDM2");

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-fallback")
                .alignedExpertObjects(null)
                .expertObjects(List.of(eo1, eo2, eo3))
                .build();

        adapter.executeFindRoles(config);

        assertTrue(restClient.findRolesCalled.get());
        assertEquals(List.of("TP53", "MDM2"), restClient.capturedRequest.get().alignedObjects());
    }

    @Test
    @DisplayName("executeFindRoles should pass empty list when both alignedExpertObjects and expertObjects are null")
    void executeFindRoles_bothNull_sendsEmptyList() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-empty")
                .alignedExpertObjects(null)
                .expertObjects(null)
                .build();

        adapter.executeFindRoles(config);

        assertTrue(restClient.findRolesCalled.get());
        assertEquals(List.of(), restClient.capturedRequest.get().alignedObjects());
    }
}
