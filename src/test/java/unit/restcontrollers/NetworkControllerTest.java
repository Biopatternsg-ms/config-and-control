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

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.port.in.CreateNetwork;
import com.biopatternsg.domain.port.in.FindNetwork;
import com.biopatternsg.domain.port.in.UpdateNetwork;
import com.biopatternsg.infrastructure.adapters.in.restcontrollers.NetworkController;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.dtos.NetworkResponse;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkControllerTest {

    static class FakeCreateNetwork implements CreateNetwork {
        NetworkConfig configToReturn;
        NetworkConfig capturedModel;

        @Override
        public NetworkConfig execute(NetworkConfig networkConfig) {
            this.capturedModel = networkConfig;
            return configToReturn;
        }
    }

    static class FakeUpdateNetwork implements UpdateNetwork {
        NetworkConfig configToReturn;
        NetworkConfig capturedModel;

        @Override
        public NetworkConfig execute(NetworkConfig networkConfig) {
            this.capturedModel = networkConfig;
            return configToReturn;
        }
    }

    static class FakeFindNetwork implements FindNetwork {
        NetworkConfig configById;
        ReportFormat<NetworkConfig> reportToReturn;

        @Override
        public NetworkConfig byId(String id) {
            return configById;
        }

        @Override
        public NetworkConfig byName(String name) {
            return null;
        }

        @Override
        public ReportFormat<NetworkConfig> byFilters(NetworkConfig filter, int page, int size) {
            return reportToReturn;
        }
    }

    private FakeCreateNetwork fakeCreate;
    private FakeUpdateNetwork fakeUpdate;
    private FakeFindNetwork fakeFind;
    private NetworkController controller;

    @BeforeEach
    void setUp() {
        fakeCreate = new FakeCreateNetwork();
        fakeUpdate = new FakeUpdateNetwork();
        fakeFind = new FakeFindNetwork();
        controller = new NetworkController(fakeCreate, fakeUpdate, fakeFind);
    }

    @Test
    @DisplayName("create should return HTTP 201 with NetworkResponse entity")
    void create_returnsNetworkResponse() {
        fakeCreate.configToReturn = NetworkConfig.builder()
                .id("net-100")
                .userId("user-1")
                .name("Cancer Network")
                .description("Sample net")
                .createdAt(1700000000)
                .build();

        CreateNetworkRequest request = new CreateNetworkRequest("Cancer Network", "Sample net");
        Response response = controller.create(request);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof NetworkResponse);

        NetworkResponse entity = (NetworkResponse) response.getEntity();
        assertEquals("net-100", entity.id());
        assertEquals("Cancer Network", entity.name());
        assertEquals("user-1", entity.userId());
    }

    @Test
    @DisplayName("update should return NetworkResponse directly")
    void update_returnsNetworkResponse() {
        fakeUpdate.configToReturn = NetworkConfig.builder()
                .id("net-100")
                .userId("user-1")
                .name("Updated Net")
                .description("Updated desc")
                .createdAt(1700000000)
                .build();

        UpdateNetworkRequest request = new UpdateNetworkRequest("net-100", "Updated Net", "Updated desc");
        NetworkResponse response = controller.update(request);

        assertNotNull(response);
        assertEquals("net-100", response.id());
        assertEquals("Updated Net", response.name());
    }

    @Test
    @DisplayName("findList by ID should return NetworkResponse directly")
    void findListById_returnsNetworkResponse() {
        fakeFind.configById = NetworkConfig.builder()
                .id("net-200")
                .userId("user-2")
                .name("Immune Net")
                .description("Desc")
                .createdAt(1700001000)
                .build();

        NetworkResponse response = controller.findList("net-200");

        assertNotNull(response);
        assertEquals("net-200", response.id());
        assertEquals("Immune Net", response.name());
    }

    @Test
    @DisplayName("findList with filters should return ReportFormat<NetworkResponse>")
    void findListWithFilters_returnsReportFormatNetworkResponse() {
        fakeFind.reportToReturn = new ReportFormat<>(
                1L,
                List.of(NetworkConfig.builder()
                        .id("net-300")
                        .userId("user-3")
                        .name("Metabolic Net")
                        .description("Desc")
                        .createdAt(1700002000)
                        .build())
        );

        FindNetworkRequest request = new FindNetworkRequest("net-300", "Metabolic Net", "Desc", 1, 10);
        ReportFormat<NetworkResponse> report = controller.findList(request);

        assertNotNull(report);
        assertEquals(1L, report.count());
        assertEquals(1, report.list().size());
        assertEquals("net-300", report.list().get(0).id());
        assertEquals("Metabolic Net", report.list().get(0).name());
    }
}
