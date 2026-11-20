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

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.adapters.mappers.PipelineMapper;
import com.biopatternsg.infrastructure.dtos.CreatePipelineRequest;
import com.biopatternsg.infrastructure.dtos.FindPipelineRequest;
import com.biopatternsg.infrastructure.dtos.PipelineDetailResponse;
import com.biopatternsg.infrastructure.dtos.PipelineResponse;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineMapperTest {

    @Test
    @DisplayName("PipelineMapper should have private constructor preventing instantiation")
    void constructor_shouldBePrivateAndThrowException() throws Exception {
        Constructor<PipelineMapper> constructor = PipelineMapper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    @DisplayName("symbolsToExpertObjects should transform string symbols into ExpertObjectConfig list")
    void symbolsToExpertObjects_shouldMapCorrectly() {
        assertTrue(PipelineMapper.symbolsToExpertObjects(null).isEmpty());
        assertTrue(PipelineMapper.symbolsToExpertObjects(List.of()).isEmpty());

        List<ExpertObjectConfig> result = PipelineMapper.symbolsToExpertObjects(List.of("TP53", "BRCA1", "EGFR"));
        assertEquals(3, result.size());
        assertEquals("TP53", result.get(0).getSymbol());
        assertEquals("BRCA1", result.get(1).getSymbol());
        assertEquals("EGFR", result.get(2).getSymbol());
    }

    @Test
    @DisplayName("symbolToExpertObject should handle null and return populated config")
    void symbolToExpertObject_shouldMapCorrectly() {
        assertNull(PipelineMapper.symbolToExpertObject(null));

        ExpertObjectConfig config = PipelineMapper.symbolToExpertObject("MYC");
        assertNotNull(config);
        assertEquals("MYC", config.getSymbol());
    }

    @Test
    @DisplayName("collectionToModel and alias collectionToConfig should map collection to domain model")
    void collectionToModel_shouldMapCorrectly() {
        assertNull(PipelineMapper.collectionToModel(null));
        assertNull(PipelineMapper.collectionToConfig(null));

        ObjectId objectId = new ObjectId();
        PipelineCollection collection = PipelineCollection.builder()
                .networkId("net-1")
                .name("Pipe 1")
                .description("Pipeline Desc")
                .step(PipelineSteps.CONFIG)
                .build();
        collection.id = objectId;

        PipelineConfig model = PipelineMapper.collectionToModel(collection);
        assertNotNull(model);
        assertEquals(objectId.toString(), model.getId());
        assertEquals("net-1", model.getNetworkId());
        assertEquals("Pipe 1", model.getName());
        assertEquals(PipelineSteps.CONFIG, model.getStep());

        PipelineConfig aliasModel = PipelineMapper.collectionToConfig(collection);
        assertEquals(model.getId(), aliasModel.getId());
    }

    @Test
    @DisplayName("modelToResponse and modelToDetailResponse should map correctly")
    void responseMappers_shouldMapCorrectly() {
        assertNull(PipelineMapper.modelToResponse(null));
        assertNull(PipelineMapper.modelToDetailResponse(null));

        PipelineConfig model = PipelineConfig.builder()
                .id(new ObjectId().toString())
                .networkId("net-1")
                .name("Pipe A")
                .description("Desc A")
                .step(PipelineSteps.CONFIG)
                .levels(3)
                .build();

        PipelineResponse response = PipelineMapper.modelToResponse(model);
        assertNotNull(response);
        assertEquals(model.getId(), response.id());
        assertEquals("Pipe A", response.name());

        PipelineDetailResponse detailResponse = PipelineMapper.modelToDetailResponse(model);
        assertNotNull(detailResponse);
        assertEquals(model.getId(), detailResponse.id());
        assertEquals(3, detailResponse.levels());
    }

    @Test
    @DisplayName("toDomainReport should transform database report envelope to domain report envelope")
    void toDomainReport_shouldTransformCorrectly() {
        ReportFormat<PipelineConfig> nullReport = PipelineMapper.toDomainReport(null);
        assertNotNull(nullReport);
        assertEquals(0L, nullReport.count());
        assertTrue(nullReport.list().isEmpty());

        ObjectId objectId = new ObjectId();
        PipelineCollection collection = PipelineCollection.builder()
                .name("Pipe B")
                .networkId("net-2")
                .build();
        collection.id = objectId;

        ReportFormat<PipelineCollection> dbReport = new ReportFormat<>(5L, List.of(collection));
        ReportFormat<PipelineConfig> domainReport = PipelineMapper.toDomainReport(dbReport);
        assertNotNull(domainReport);
        assertEquals(5L, domainReport.count());
        assertEquals(1, domainReport.list().size());
        assertEquals("Pipe B", domainReport.list().getFirst().getName());
    }

    @Test
    @DisplayName("requestToModel should map CreatePipelineRequest and FindPipelineRequest")
    void requestToModel_shouldMapSafely() {
        assertNull(PipelineMapper.requestToCreate(null));
        assertNull(PipelineMapper.requestToModel((CreatePipelineRequest) null));
        assertNull(PipelineMapper.requestToModel((FindPipelineRequest) null));

        CreatePipelineRequest create = new CreatePipelineRequest("Pipe New", "Desc New", "net-3");
        PipelineConfig createdModel = PipelineMapper.requestToModel(create);
        assertEquals("Pipe New", createdModel.getName());
        assertEquals("net-3", createdModel.getNetworkId());

        FindPipelineRequest find = new FindPipelineRequest("pipe-id", "net-4", "Search", "Desc", 0, 10);
        PipelineConfig findModel = PipelineMapper.requestToModel(find);
        assertEquals("pipe-id", findModel.getId());
        assertEquals("Search", findModel.getName());
    }
}
