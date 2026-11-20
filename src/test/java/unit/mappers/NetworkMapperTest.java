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
package unit.mappers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.adapters.mappers.NetworkMapper;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.dtos.NetworkResponse;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkMapperTest {

    @Test
    @DisplayName("NetworkMapper should have private constructor preventing instantiation")
    void constructor_shouldBePrivateAndThrowException() throws Exception {
        Constructor<NetworkMapper> constructor = NetworkMapper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    @DisplayName("collectionToModel and alias toNetworkConfig should handle null and map valid collection")
    void collectionToModel_shouldMapCorrectly() {
        assertNull(NetworkMapper.collectionToModel(null));
        assertNull(NetworkMapper.toNetworkConfig(null));

        ObjectId objectId = new ObjectId();
        NetworkCollection collection = NetworkCollection.builder()
                .userId("user-1")
                .name("Cancer Network")
                .description("Desc")
                .build();
        collection.id = objectId;

        NetworkConfig model = NetworkMapper.collectionToModel(collection);
        assertNotNull(model);
        assertEquals(objectId.toString(), model.getId());
        assertEquals("user-1", model.getUserId());
        assertEquals("Cancer Network", model.getName());
        assertEquals("Desc", model.getDescription());

        NetworkConfig aliasModel = NetworkMapper.toNetworkConfig(collection);
        assertEquals(model.getId(), aliasModel.getId());
    }

    @Test
    @DisplayName("modelToResponse and alias toNetworkResponse should handle null and map valid model")
    void modelToResponse_shouldMapCorrectly() {
        assertNull(NetworkMapper.modelToResponse(null));
        assertNull(NetworkMapper.toNetworkResponse(null));

        NetworkConfig model = NetworkConfig.builder()
                .id(new ObjectId().toString())
                .userId("user-1")
                .name("Net A")
                .description("Desc A")
                .createdAt(12345)
                .build();

        NetworkResponse response = NetworkMapper.modelToResponse(model);
        assertNotNull(response);
        assertEquals(model.getId(), response.id());
        assertEquals(model.getUserId(), response.userId());
        assertEquals("Net A", response.name());
        assertEquals("Desc A", response.description());

        NetworkResponse aliasResponse = NetworkMapper.toNetworkResponse(model);
        assertEquals(response.id(), aliasResponse.id());
    }

    @Test
    @DisplayName("modelToCollection and alias toNetworkCollection should handle null and map valid model")
    void modelToCollection_shouldMapCorrectly() {
        assertNull(NetworkMapper.modelToCollection(null, "user-1"));
        assertNull(NetworkMapper.toNetworkCollection(null, "user-1"));

        ObjectId objectId = new ObjectId();
        NetworkConfig model = NetworkConfig.builder()
                .id(objectId.toString())
                .name("Net B")
                .description("Desc B")
                .build();

        NetworkCollection collection = NetworkMapper.modelToCollection(model, "user-2");
        assertNotNull(collection);
        assertEquals(objectId, collection.id);
        assertEquals("user-2", collection.getUserId());
        assertEquals("Net B", collection.getName());
        assertEquals("Desc B", collection.getDescription());
    }

    @Test
    @DisplayName("toDomainReport should handle null, empty, and populated envelopes")
    void toDomainReport_shouldTransformCorrectly() {
        ReportFormat<NetworkConfig> nullReport = NetworkMapper.toDomainReport(null);
        assertNotNull(nullReport);
        assertEquals(0L, nullReport.count());
        assertTrue(nullReport.list().isEmpty());

        ObjectId id = new ObjectId();
        NetworkCollection collection = NetworkCollection.builder()
                .userId("user-1")
                .name("Net C")
                .build();
        collection.id = id;

        ReportFormat<NetworkCollection> dbReport = new ReportFormat<>(1L, List.of(collection));
        ReportFormat<NetworkConfig> domainReport = NetworkMapper.toDomainReport(dbReport);
        assertNotNull(domainReport);
        assertEquals(1L, domainReport.count());
        assertEquals(1, domainReport.list().size());
        assertEquals("Net C", domainReport.list().getFirst().getName());
    }

    @Test
    @DisplayName("requestToModel should map create, update, and find requests safely")
    void requestToModel_shouldMapSafely() {
        assertNull(NetworkMapper.requestToModel((CreateNetworkRequest) null));
        assertNull(NetworkMapper.requestToModel((UpdateNetworkRequest) null));
        assertNull(NetworkMapper.requestToModel((FindNetworkRequest) null));

        CreateNetworkRequest create = new CreateNetworkRequest("Net 1", "Desc 1");
        NetworkConfig createModel = NetworkMapper.requestToModel(create);
        assertEquals("Net 1", createModel.getName());
        assertEquals("Desc 1", createModel.getDescription());

        UpdateNetworkRequest update = new UpdateNetworkRequest("id-1", "Net 2", "Desc 2");
        NetworkConfig updateModel = NetworkMapper.requestToModel(update);
        assertEquals("id-1", updateModel.getId());
        assertEquals("Net 2", updateModel.getName());

        FindNetworkRequest find = new FindNetworkRequest("id-2", "Net 3", "Desc 3", 0, 10);
        NetworkConfig findModel = NetworkMapper.requestToModel(find);
        assertEquals("id-2", findModel.getId());
        assertEquals("Net 3", findModel.getName());
    }
}
