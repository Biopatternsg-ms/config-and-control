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
import com.biopatternsg.domain.port.out.PipelineStepPort;
import com.biopatternsg.domain.port.out.repositories.PipelineRepository;
import com.biopatternsg.infrastructure.adapters.out.repositories.InferencesAdapter;
import com.biopatternsg.infrastructure.adapters.out.repositories.PubmedIntegrationAdapter;
import com.biopatternsg.infrastructure.clients.InferencesRestClient;
import com.biopatternsg.infrastructure.clients.PubmedRestClient;
import com.biopatternsg.infrastructure.dtos.BuildPairsRequest;
import com.biopatternsg.infrastructure.dtos.FindRolesRequest;
import com.biopatternsg.infrastructure.dtos.GenerateKbRequest;
import com.biopatternsg.infrastructure.dtos.SearchPubmedIdsRequest;
import com.biopatternsg.infrastructure.dtos.SearchPubtatorRequest;
import com.biopatternsg.infrastructure.dtos.GenerateAlignedObjectsRequest;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OutboundAdaptersFailFastTest {

    static class RecordingPipelineRepository implements PipelineRepository, PipelineStepPort {
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
        public PipelineConfig findByName(String name) { return null; }

        @Override
        public PipelineConfig findByNameExists(String networkId, String name) { return null; }

        @Override
        public com.biopatternsg.domain.models.ReportFormat<PipelineConfig> findByFilters(PipelineConfig findPipeline, int page, int size) { return null; }

        @Override
        public List<PipelineConfig> findAllFailed() { return new ArrayList<>(storage.values()); }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status) {
            return updateStep(id, step, status, null);
        }

        @Override
        public PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics) {
            var pipeline = storage.get(id);
            if (pipeline != null) {
                pipeline.setStep(step);
                pipeline.addStatusForStep(step, status, metrics);
                save(pipeline);
            }
            return pipeline;
        }
    }

    static class FailingPubmedRestClient implements PubmedRestClient {
        @Override
        public void buildPairs(BuildPairsRequest request, String userId) {
            throw new WebApplicationException("Pubmed server error", Response.Status.INTERNAL_SERVER_ERROR);
        }

        @Override
        public void searchPubmedIds(SearchPubmedIdsRequest request, String userId) {
            throw new ProcessingException("Connection refused to PubMed service");
        }

        @Override
        public void searchPubtator(SearchPubtatorRequest request, String userId) {
            throw new WebApplicationException("PubTator timeout", Response.Status.GATEWAY_TIMEOUT);
        }

        @Override
        public void generateKb(GenerateKbRequest request, String userId) {
            throw new WebApplicationException("Knowledge base error", Response.Status.SERVICE_UNAVAILABLE);
        }

        @Override
        public void generateAlignedObjects(GenerateAlignedObjectsRequest request, String userId) {
            throw new WebApplicationException("Alignment error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    static class FailingInferencesRestClient implements InferencesRestClient {
        @Override
        public void findRoles(FindRolesRequest request, String userId) {
            throw new ProcessingException("Inferences service socket timeout");
        }
    }

    private RecordingPipelineRepository pipelineRepository;
    private SessionUtil sessionUtil;

    @BeforeEach
    void setUp() {
        pipelineRepository = new RecordingPipelineRepository();

        sessionUtil = new SessionUtil();
        MultivaluedMap<String, String> context = new MultivaluedHashMap<>();
        context.putSingle("x-user-id", "test-user-id");
        sessionUtil.setContext(context);
    }

    @Test
    @DisplayName("PubmedIntegrationAdapter catches 500 error and transitions step to FAILED")
    void pubmedAdapter_failsFastOn500() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-pubmed-500")
                .step(PipelineSteps.SEARCH_LEVELS)
                .statuses(new ArrayList<>())
                .build();
        pipelineRepository.save(pipeline);

        PubmedIntegrationAdapter adapter = new PubmedIntegrationAdapter(
                new FailingPubmedRestClient(),
                pipelineRepository,
                sessionUtil
        );

        adapter.executeBuildsPairs(pipeline);

        PipelineConfig updated = pipelineRepository.findById("pipe-pubmed-500");
        assertNotNull(updated);
        assertEquals(PipelineSteps.COMBINATIONS, updated.getStep());
        assertEquals(Status.FAILED, updated.getStatuses().get(updated.getStatuses().size() - 1).getStatus());
    }

    @Test
    @DisplayName("PubmedIntegrationAdapter catches ProcessingException (timeout) and transitions step to FAILED")
    void pubmedAdapter_failsFastOnTimeout() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-pubmed-timeout")
                .step(PipelineSteps.COMBINATIONS)
                .statuses(new ArrayList<>())
                .build();
        pipelineRepository.save(pipeline);

        PubmedIntegrationAdapter adapter = new PubmedIntegrationAdapter(
                new FailingPubmedRestClient(),
                pipelineRepository,
                sessionUtil
        );

        adapter.executeSearchPubmedIds(pipeline);

        PipelineConfig updated = pipelineRepository.findById("pipe-pubmed-timeout");
        assertNotNull(updated);
        assertEquals(PipelineSteps.SEARCH_PUBMED_IDS, updated.getStep());
        assertEquals(Status.FAILED, updated.getStatuses().get(updated.getStatuses().size() - 1).getStatus());
    }

    @Test
    @DisplayName("InferencesAdapter catches timeout and transitions FIND_ROLES step to FAILED")
    void inferencesAdapter_failsFastOnTimeout() {
        PipelineConfig pipeline = PipelineConfig.builder()
                .id("pipe-inferences-timeout")
                .step(PipelineSteps.CONFIGURE_INFERENCES)
                .alignedExpertObjects(List.of("BRCA1", "TP53"))
                .statuses(new ArrayList<>())
                .build();
        pipelineRepository.save(pipeline);

        InferencesAdapter adapter = new InferencesAdapter(
                new FailingInferencesRestClient(),
                pipelineRepository,
                sessionUtil
        );

        adapter.executeFindRoles(pipeline);

        PipelineConfig updated = pipelineRepository.findById("pipe-inferences-timeout");
        assertNotNull(updated);
        assertEquals(PipelineSteps.FIND_ROLES, updated.getStep());
        assertEquals(Status.FAILED, updated.getStatuses().get(updated.getStatuses().size() - 1).getStatus());
    }
}
