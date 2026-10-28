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
package unit.restcontrollers;

import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.models.PipelineResumeSummary;
import com.biopatternsg.domain.port.in.ResumePipeline;
import com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController;
import com.biopatternsg.infrastructure.dtos.PipelineResumeResponse;
import com.biopatternsg.infrastructure.dtos.ResumePipelineRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineControllerTest {

    static class FakeResumePipeline implements ResumePipeline {
        MicroService capturedService;
        PipelineResumeSummary summaryToReturn;

        @Override
        public PipelineResumeSummary execute(MicroService microService) {
            this.capturedService = microService;
            return summaryToReturn;
        }
    }

    private FakeResumePipeline fakeResumePipeline;
    private PipelineController controller;

    @BeforeEach
    void setUp() {
        fakeResumePipeline = new FakeResumePipeline();
        controller = new PipelineController(
                null, // createPipeline
                null, // launchPipeline
                null, // updatePipeline
                null, // findPipeline
                null, // updatePipelineStep
                null, // getPipelineExecution
                null, // pipelineService
                null, // triggerPubmedIntegration
                fakeResumePipeline
        );
    }

    @Test
    @DisplayName("resume should return HTTP 200 with PipelineResumeResponse when request is valid for SEARCH_BIOLOGICAL_OBJECTS")
    void resume_successfulBiologicalObjects() {
        fakeResumePipeline.summaryToReturn = PipelineResumeSummary.builder()
                .microService(MicroService.SEARCH_BIOLOGICAL_OBJECTS)
                .totalFound(2)
                .resumedCount(2)
                .failedCount(0)
                .resumedPipelineIds(List.of("pipe-1", "pipe-2"))
                .failedPipelineIds(List.of())
                .errors(Map.of())
                .build();

        ResumePipelineRequest request = new ResumePipelineRequest(MicroService.SEARCH_BIOLOGICAL_OBJECTS);
        Response response = controller.resume(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof PipelineResumeResponse);

        PipelineResumeResponse body = (PipelineResumeResponse) response.getEntity();
        assertEquals(MicroService.SEARCH_BIOLOGICAL_OBJECTS, body.microService());
        assertEquals(2, body.totalFound());
        assertEquals(2, body.resumedCount());
        assertEquals(0, body.failedCount());
        assertEquals(List.of("pipe-1", "pipe-2"), body.resumedPipelineIds());
        assertEquals(MicroService.SEARCH_BIOLOGICAL_OBJECTS, fakeResumePipeline.capturedService);
    }

    @Test
    @DisplayName("resume should return HTTP 200 with PipelineResumeResponse when request is valid for PUBMED_INTEGRATION")
    void resume_successfulPubmedIntegration() {
        fakeResumePipeline.summaryToReturn = PipelineResumeSummary.builder()
                .microService(MicroService.PUBMED_INTEGRATION)
                .totalFound(1)
                .resumedCount(1)
                .failedCount(0)
                .resumedPipelineIds(List.of("pipe-pm-1"))
                .failedPipelineIds(List.of())
                .errors(Map.of())
                .build();

        ResumePipelineRequest request = new ResumePipelineRequest(MicroService.PUBMED_INTEGRATION);
        Response response = controller.resume(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof PipelineResumeResponse);

        PipelineResumeResponse body = (PipelineResumeResponse) response.getEntity();
        assertEquals(MicroService.PUBMED_INTEGRATION, body.microService());
        assertEquals(1, body.totalFound());
        assertEquals(1, body.resumedCount());
        assertEquals(MicroService.PUBMED_INTEGRATION, fakeResumePipeline.capturedService);
    }

    @Test
    @DisplayName("resume should return HTTP 400 Bad Request when request is null")
    void resume_nullRequest_returnsBadRequest() {
        Response response = controller.resume(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNull(fakeResumePipeline.capturedService);
    }

    @Test
    @DisplayName("resume should return HTTP 400 Bad Request when microService is null")
    void resume_nullMicroService_returnsBadRequest() {
        ResumePipelineRequest request = new ResumePipelineRequest(null);
        Response response = controller.resume(request);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNull(fakeResumePipeline.capturedService);
    }
}
