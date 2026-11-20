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

import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.infrastructure.adapters.mappers.UserMapper;
import com.biopatternsg.infrastructure.dtos.UserFiltersRequest;
import com.biopatternsg.infrastructure.dtos.UserRequest;
import com.biopatternsg.infrastructure.dtos.UserResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserKeycloakResponse;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    @DisplayName("UserMapper should have private constructor preventing instantiation")
    void constructor_shouldBePrivateAndThrowException() throws Exception {
        Constructor<UserMapper> constructor = UserMapper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    @DisplayName("collectionToModel and modelToCollection should map correctly")
    void collectionAndModel_shouldMapCorrectly() {
        assertNull(UserMapper.collectionToModel(null));
        assertNull(UserMapper.modelToCollection(null));

        ObjectId objectId = new ObjectId();
        UserCollection collection = UserCollection.builder()
                .identityProviderId("keycloak-123")
                .username("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .build();
        collection.id = objectId;

        UserConfig model = UserMapper.collectionToModel(collection);
        assertNotNull(model);
        assertEquals(objectId.toString(), model.getId());
        assertEquals("keycloak-123", model.getIdentityProviderId());
        assertEquals("john.doe@example.com", model.getUsername());
        assertTrue(model.isEnabled());

        UserCollection mappedBack = UserMapper.modelToCollection(model);
        assertNotNull(mappedBack);
        assertEquals(objectId, mappedBack.id);
        assertEquals("john.doe@example.com", mappedBack.getUsername());
    }

    @Test
    @DisplayName("modelToResponse should map user domain model to response DTO")
    void modelToResponse_shouldMapCorrectly() {
        assertNull(UserMapper.modelToResponse(null));

        UserConfig model = UserConfig.builder()
                .id(new ObjectId().toString())
                .username("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .enabled(true)
                .createdAt(54321)
                .build();

        UserResponse response = UserMapper.modelToResponse(model);
        assertNotNull(response);
        assertEquals(model.getId(), response.id());
        assertEquals("alice@example.com", response.username());
        assertEquals("Alice", response.firstName());
        assertEquals("Smith", response.lastName());
        assertTrue(response.enabled());
    }

    @Test
    @DisplayName("toDomainReport should transform database report envelope to domain report envelope")
    void toDomainReport_shouldTransformCorrectly() {
        ReportFormat<UserConfig> nullReport = UserMapper.toDomainReport(null);
        assertNotNull(nullReport);
        assertEquals(0L, nullReport.count());
        assertTrue(nullReport.list().isEmpty());

        ObjectId objectId = new ObjectId();
        UserCollection collection = UserCollection.builder()
                .username("bob@example.com")
                .firstName("Bob")
                .build();
        collection.id = objectId;

        ReportFormat<UserCollection> dbReport = new ReportFormat<>(3L, List.of(collection));
        ReportFormat<UserConfig> domainReport = UserMapper.toDomainReport(dbReport);
        assertNotNull(domainReport);
        assertEquals(3L, domainReport.count());
        assertEquals(1, domainReport.list().size());
        assertEquals("bob@example.com", domainReport.list().getFirst().getUsername());
    }

    @Test
    @DisplayName("requestToModel should map UserRequest, UserFiltersRequest, and UserKeycloakResponse")
    void requestToModel_shouldMapSafely() {
        assertNull(UserMapper.requestToModel((UserRequest) null));
        assertNull(UserMapper.requestToModel((UserFiltersRequest) null));
        assertNull(UserMapper.keycloakToModel(null));

        UserRequest request = new UserRequest("carol@example.com", "Carol", "Danvers");
        UserConfig userConfig = UserMapper.requestToModel(request);
        assertEquals("carol@example.com", userConfig.getUsername());
        assertEquals("Carol", userConfig.getFirstName());

        UserFiltersRequest filters = new UserFiltersRequest("carol", "Carol", null, true, 0, 10);
        UserConfig filterConfig = UserMapper.requestToModel(filters);
        assertEquals("carol", filterConfig.getUsername());
        assertEquals("Carol", filterConfig.getFirstName());

        UserKeycloakResponse kc = UserKeycloakResponse.builder()
                .id("kc-999")
                .username("dan")
                .firstName("Dan")
                .lastName("V")
                .enabled(true)
                .build();
        UserConfig kcConfig = UserMapper.keycloakToModel(kc);
        assertEquals("kc-999", kcConfig.getIdentityProviderId());
        assertEquals("dan", kcConfig.getUsername());
    }
}
