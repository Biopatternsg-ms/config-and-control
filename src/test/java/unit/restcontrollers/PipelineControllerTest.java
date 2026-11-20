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
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.UpdatePipelineEnum;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineResumeSummary;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.in.CreatePipeline;
import com.biopatternsg.domain.port.in.FindPipeline;
import com.biopatternsg.domain.port.in.ResumePipeline;
import com.biopatternsg.domain.port.in.UpdatePipeline;
import com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController;
import com.biopatternsg.infrastructure.dtos.*;
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

    static class FakeCreatePipeline implements CreatePipeline {
        PipelineConfig configToReturn;

        @Override
        public PipelineConfig execute(PipelineConfig createPipeline) {
            return configToReturn;
        }
    }

    static class FakeUpdatePipeline implements UpdatePipeline {
        PipelineConfig configToReturn;
        UpdatePipelineEnum capturedEnum;

        @Override
        public PipelineConfig execute(PipelineConfig updatePipeline, UpdatePipelineEnum pipelineEnum) {
            this.capturedEnum = pipelineEnum;
            return configToReturn;
        }
    }

    static class FakeFindPipeline implements FindPipeline {
        PipelineConfig configById;
        ReportFormat<PipelineConfig> reportToReturn;

        @Override
        public PipelineConfig byId(String id) {
            return configById;
        }

        @Override
        public PipelineConfig byName(String name) {
            return null;
        }

        @Override
        public ReportFormat<PipelineConfig> byFilters(PipelineConfig findPipeline, int page, int size) {
            return reportToReturn;
        }
    }

    private FakeResumePipeline fakeResumePipeline;
    private FakeCreatePipeline fakeCreatePipeline;
    private FakeUpdatePipeline fakeUpdatePipeline;
    private FakeFindPipeline fakeFindPipeline;
    private PipelineController controller;

    @BeforeEach
    void setUp() {
        fakeResumePipeline = new FakeResumePipeline();
        fakeCreatePipeline = new FakeCreatePipeline();
        fakeUpdatePipeline = new FakeUpdatePipeline();
        fakeFindPipeline = new FakeFindPipeline();
        controller = new PipelineController(
                fakeCreatePipeline,
                null, // launchPipeline
                fakeUpdatePipeline,
                fakeFindPipeline,
                null, // updatePipelineStep
                null, // getPipelineExecution
                null, // regenerateAlignedObjects
                fakeResumePipeline
        );
    }

    @Test
    @DisplayName("create should return HTTP 201 with PipelineDetailResponse entity")
    void create_returnsPipelineDetailResponse() {
        fakeCreatePipeline.configToReturn = PipelineConfig.builder()
                .id("pipe-create-1")
                .name("New Pipeline")
                .description("Pipeline desc")
                .networkId("net-1")
                .step(PipelineSteps.CONFIG)
                .createdAt(1700000000)
                .build();

        CreatePipelineRequest request = new CreatePipelineRequest("net-1", "New Pipeline", "Pipeline desc");
        Response response = controller.create(request);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof PipelineDetailResponse);

        PipelineDetailResponse body = (PipelineDetailResponse) response.getEntity();
        assertEquals("pipe-create-1", body.id());
        assertEquals("New Pipeline", body.name());
        assertEquals("net-1", body.networkId());
    }

    @Test
    @DisplayName("updateDescription should return PipelineDetailResponse directly")
    void updateDescription_returnsPipelineDetailResponse() {
        fakeUpdatePipeline.configToReturn = PipelineConfig.builder()
                .id("pipe-update-1")
                .name("Existing Pipe")
                .description("Updated description")
                .networkId("net-1")
                .step(PipelineSteps.CONFIG)
                .build();

        UpdatePipelineDescriptionRequest request = new UpdatePipelineDescriptionRequest(
                "pipe-update-1",
                "Existing Pipe",
                "Updated description"
        );
        PipelineDetailResponse response = controller.updateDescription(request);

        assertNotNull(response);
        assertEquals("pipe-update-1", response.id());
        assertEquals("Updated description", response.description());
        assertEquals(UpdatePipelineEnum.DESCRIPTION, fakeUpdatePipeline.capturedEnum);
    }

    @Test
    @DisplayName("findList by ID should return PipelineDetailResponse directly")
    void findListById_returnsPipelineDetailResponse() {
        fakeFindPipeline.configById = PipelineConfig.builder()
                .id("pipe-find-1")
                .name("Found Pipe")
                .description("Found desc")
                .networkId("net-1")
                .step(PipelineSteps.CONFIG)
                .build();

        PipelineDetailResponse response = controller.findList("pipe-find-1");

        assertNotNull(response);
        assertEquals("pipe-find-1", response.id());
        assertEquals("Found Pipe", response.name());
    }

    @Test
    @DisplayName("findList with filters should return ReportFormat<PipelineResponse>")
    void findListWithFilters_returnsReportFormatPipelineResponse() {
        fakeFindPipeline.reportToReturn = new ReportFormat<>(
                1L,
                List.of(PipelineConfig.builder()
                        .id("pipe-list-1")
                        .name("List Pipe")
                        .description("List desc")
                        .networkId("net-1")
                        .step(PipelineSteps.CONFIG)
                        .build())
        );

        FindPipelineRequest request = new FindPipelineRequest(
                "pipe-list-1", "net-1", "List Pipe", "List desc", 1, 10
        );
        ReportFormat<PipelineResponse> report = controller.findList(request);

        assertNotNull(report);
        assertEquals(1L, report.count());
        assertEquals(1, report.list().size());
        assertEquals("pipe-list-1", report.list().get(0).id());
        assertEquals("List Pipe", report.list().get(0).name());
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
}
