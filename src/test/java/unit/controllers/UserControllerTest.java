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

import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.domain.port.in.UserManagement;
import com.biopatternsg.infrastructure.adapters.in.restcontrollers.UserController;
import com.biopatternsg.infrastructure.dtos.keycloak.KeycloakEventNotification;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    static class FakeUserManagement implements UserManagement {
        AtomicBoolean processEmailVerificationCalled = new AtomicBoolean(false);
        AtomicReference<String> processedKeycloakId = new AtomicReference<>();

        @Override
        public void register(UserConfig request) {}

        @Override
        public void syncUsers() {}

        @Override
        public void recoveryPassword(String email) {}

        @Override
        public UserConfig updateStatus(String id, boolean enabled) {
            return null;
        }

        @Override
        public void processEmailVerification(String keycloakId) {
            processEmailVerificationCalled.set(true);
            processedKeycloakId.set(keycloakId);
        }

        @Override
        public ReportFormat<UserConfig> listUsers(UserConfig filters, int page, int size) {
            return null;
        }
    }

    @Test
    void shouldProcessVerifyEmailEvent() {
        FakeUserManagement fakeUserManagement = new FakeUserManagement();
        UserController controller = new UserController(null, fakeUserManagement);

        KeycloakEventNotification notification = KeycloakEventNotification.builder()
                .type("VERIFY_EMAIL")
                .userId("test-user-uuid-123")
                .realmId("biopatternsg")
                .build();

        Response response = controller.handleKeycloakEvent(notification);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(fakeUserManagement.processEmailVerificationCalled.get());
        assertEquals("test-user-uuid-123", fakeUserManagement.processedKeycloakId.get());
    }

    @Test
    void shouldIgnoreNonVerifyEmailEvent() {
        FakeUserManagement fakeUserManagement = new FakeUserManagement();
        UserController controller = new UserController(null, fakeUserManagement);

        KeycloakEventNotification notification = KeycloakEventNotification.builder()
                .type("LOGIN")
                .userId("test-user-uuid-123")
                .realmId("biopatternsg")
                .build();

        Response response = controller.handleKeycloakEvent(notification);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertFalse(fakeUserManagement.processEmailVerificationCalled.get());
    }
}
