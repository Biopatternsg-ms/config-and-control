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
package unit.controllers;

import com.biopatternsg.domain.models.ResumeFailedPipelinesResponse;
import com.biopatternsg.domain.port.in.ResumeFailedPipelines;
import com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class PipelineControllerTest {

    static class FakeResumeFailedPipelines implements ResumeFailedPipelines {
        AtomicBoolean called = new AtomicBoolean(false);
        ResumeFailedPipelinesResponse responseToReturn = ResumeFailedPipelinesResponse.builder()
                .totalFailed(2)
                .resumed(2)
                .skipped(0)
                .errors(0)
                .details(new ArrayList<>())
                .build();

        @Override
        public ResumeFailedPipelinesResponse execute() {
            called.set(true);
            return responseToReturn;
        }
    }

    @Test
    @DisplayName("resumeFailedPipelines invokes Inbound Port and returns 200 OK with summary response")
    void resumeFailedPipelines_returnsOkWithSummary() {
        FakeResumeFailedPipelines fakeResume = new FakeResumeFailedPipelines();

        PipelineController controller = new PipelineController(
                null, // createPipeline
                null, // launchPipeline
                fakeResume, // resumeFailedPipelines
                null, // updatePipeline
                null, // findPipeline
                null, // updatePipelineStep
                null, // getPipelineExecution
                null, // pipelineStepPort
                null  // triggerPubmedIntegration
        );

        Response response = controller.resumeFailedPipelines();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(fakeResume.called.get());
        assertTrue(response.getEntity() instanceof ResumeFailedPipelinesResponse);

        ResumeFailedPipelinesResponse body = (ResumeFailedPipelinesResponse) response.getEntity();
        assertEquals(2, body.getTotalFailed());
        assertEquals(2, body.getResumed());
    }
}
