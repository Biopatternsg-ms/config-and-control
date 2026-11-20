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
import com.biopatternsg.infrastructure.adapters.out.repositories.InferencesAdapter;
import com.biopatternsg.infrastructure.clients.InferencesRestClient;
import com.biopatternsg.infrastructure.dtos.FindRolesRequest;
import com.biopatternsg.infrastructure.dtos.PathwayInferenceRequest;
import com.biopatternsg.infrastructure.dtos.PathwayInferenceResponse;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InferencesAdapterTest {

    static class FakeInferencesRestClient implements InferencesRestClient {
        final AtomicBoolean findRolesCalled = new AtomicBoolean(false);
        final AtomicReference<FindRolesRequest> capturedRequest = new AtomicReference<>();
        final AtomicReference<String> capturedUserId = new AtomicReference<>();
        final AtomicBoolean runPathwayInferenceCalled = new AtomicBoolean(false);
        final AtomicReference<PathwayInferenceRequest> capturedPathwayRequest = new AtomicReference<>();
        boolean shouldThrow = false;
        AtomicInteger callOrder;
        int executionOrder = -1;

        @Override
        public void findRoles(FindRolesRequest request, String userId) {
            if (callOrder != null) {
                executionOrder = callOrder.incrementAndGet();
            }
            findRolesCalled.set(true);
            capturedRequest.set(request);
            capturedUserId.set(userId);
            if (shouldThrow) {
                throw new RuntimeException("Inferences microservice error");
            }
        }

        @Override
        public PathwayInferenceResponse runPathwayInference(PathwayInferenceRequest request, String userId) {
            if (callOrder != null) {
                executionOrder = callOrder.incrementAndGet();
            }
            runPathwayInferenceCalled.set(true);
            capturedPathwayRequest.set(request);
            capturedUserId.set(userId);
            if (shouldThrow) {
                throw new RuntimeException("Inferences microservice error");
            }
            return new PathwayInferenceResponse(
                    "SUCCESS",
                    request.pipelineId(),
                    17766,
                    420L,
                    100,
                    200,
                    "Completed"
            );
        }
    }

    private FakeInferencesRestClient restClient;
    private SessionUtil sessionUtil;
    private InferencesAdapter adapter;
    private AtomicInteger globalOrder;

    @BeforeEach
    void setUp() {
        globalOrder = new AtomicInteger(0);
        restClient = new FakeInferencesRestClient();
        restClient.callOrder = globalOrder;
        sessionUtil = new SessionUtil();

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("x-user-id", "test-user-123");
        sessionUtil.setContext(headers);

        adapter = new InferencesAdapter(restClient, sessionUtil);
    }

    @Test
    @DisplayName("executeFindRoles should invoke Inferences REST client with correct payload and userId")
    void executeFindRoles_callsRestClientWithPayload() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-1")
                .alignedExpertObjects(List.of("CYP7A1", "FXR"))
                .build();

        adapter.executeFindRoles(config);

        assertTrue(restClient.findRolesCalled.get());
        assertEquals("pipe-1", restClient.capturedRequest.get().pipelineId());
        assertEquals(List.of("CYP7A1", "FXR"), restClient.capturedRequest.get().alignedObjects());
        assertEquals("test-user-123", restClient.capturedUserId.get());
    }

    @Test
    @DisplayName("executeFindRoles should throw exception when Inferences REST client fails")
    void executeFindRoles_restClientThrows_rethrowsException() {
        restClient.shouldThrow = true;
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-error")
                .alignedExpertObjects(List.of("TP53"))
                .build();

        assertThrows(RuntimeException.class, () -> adapter.executeFindRoles(config));
    }

    @Test
    @DisplayName("executeFindRoles should throw exception and not invoke REST client when userId is missing")
    void executeFindRoles_missingUserId_throwsException() {
        sessionUtil.setContext(new MultivaluedHashMap<>());

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-no-user")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        assertThrows(IllegalStateException.class, () -> adapter.executeFindRoles(config));
        assertFalse(restClient.findRolesCalled.get(), "REST client must not be invoked when userId is missing");
    }

    @Test
    @DisplayName("executeFindRoles should throw exception and not invoke REST client when session context is null")
    void executeFindRoles_nullSessionContext_throwsException() {
        sessionUtil.setContext(null);

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-null-ctx")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        assertThrows(IllegalStateException.class, () -> adapter.executeFindRoles(config));
        assertFalse(restClient.findRolesCalled.get());
    }

    @Test
    @DisplayName("executeFindRoles should throw exception and not invoke REST client when userId is blank")
    void executeFindRoles_blankUserId_throwsException() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("x-user-id", "   ");
        sessionUtil.setContext(headers);

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-blank-user")
                .alignedExpertObjects(List.of("CYP7A1"))
                .build();

        assertThrows(IllegalStateException.class, () -> adapter.executeFindRoles(config));
        assertFalse(restClient.findRolesCalled.get());
    }

    @Test
    @DisplayName("executeFindRoles should fall back to ExpertObjects when alignedExpertObjects is null")
    void executeFindRoles_nullAlignedExpertObjects_fallsBackToExpertObjects() {
        ExpertObjectConfig eo1 = new ExpertObjectConfig();
        eo1.setSymbol("IL6");
        ExpertObjectConfig eo2 = new ExpertObjectConfig();
        eo2.setSymbol("TNF");

        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-fallback")
                .alignedExpertObjects(null)
                .expertObjects(List.of(eo1, eo2))
                .build();

        adapter.executeFindRoles(config);

        assertTrue(restClient.findRolesCalled.get());
        assertEquals(List.of("IL6", "TNF"), restClient.capturedRequest.get().alignedObjects());
    }

    @Test
    @DisplayName("executeFindRoles should send empty list when both alignedExpertObjects and expertObjects are null")
    void executeFindRoles_allNullObjects_sendsEmptyList() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-empty")
                .alignedExpertObjects(null)
                .expertObjects(null)
                .build();

        adapter.executeFindRoles(config);

        assertTrue(restClient.findRolesCalled.get());
        assertEquals(List.of(), restClient.capturedRequest.get().alignedObjects());
    }

    @Test
    @DisplayName("executeInferPathways should call runPathwayInference")
    void executeInferPathways_callsRestClient() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-infer-1")
                .alignedExpertObjects(List.of("IFNA1", "IRF3"))
                .build();

        adapter.executeInferPathways(config);

        assertTrue(restClient.runPathwayInferenceCalled.get());
        assertEquals("pipe-infer-1", restClient.capturedPathwayRequest.get().pipelineId());
        assertEquals(List.of("IFNA1", "IRF3"), restClient.capturedPathwayRequest.get().restrictedObjects());
        assertEquals("test-user-123", restClient.capturedUserId.get());
    }

    @Test
    @DisplayName("executeInferPathways should throw exception when REST client throws exception")
    void executeInferPathways_restClientThrows_rethrowsException() {
        restClient.shouldThrow = true;
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-infer-err")
                .alignedExpertObjects(List.of("TP53"))
                .build();

        assertThrows(RuntimeException.class, () -> adapter.executeInferPathways(config));
    }

    @Test
    @DisplayName("executeInferPathways should fall back to anonymous when user is missing")
    void executeInferPathways_missingUser_fallsBackToAnonymous() {
        sessionUtil.setContext(new MultivaluedHashMap<>());
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-anon")
                .alignedExpertObjects(List.of("TP53"))
                .build();

        adapter.executeInferPathways(config);

        assertTrue(restClient.runPathwayInferenceCalled.get());
        assertEquals("anonymous", restClient.capturedUserId.get());
        assertEquals("anonymous", restClient.capturedPathwayRequest.get().user());
    }
}
