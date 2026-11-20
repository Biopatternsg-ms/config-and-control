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

import com.biopatternsg.infrastructure.adapters.mappers.KeycloakEventMapper;
import com.biopatternsg.infrastructure.dtos.keycloak.KeycloakEventNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakEventMapperTest {

    @Test
    @DisplayName("should extract targetId when event type is VERIFY_EMAIL with valid userId")
    void extractTargetId_verifyEmail_success() {
        KeycloakEventNotification event = KeycloakEventNotification.builder()
                .type("VERIFY_EMAIL")
                .userId("user-123")
                .build();

        Optional<String> result = KeycloakEventMapper.extractEmailVerificationTargetId(event);

        assertTrue(result.isPresent());
        assertEquals("user-123", result.get());
    }

    @Test
    @DisplayName("should extract targetId when event is CUSTOM_REQUIRED_ACTION with custom_required_action=VERIFY_EMAIL")
    void extractTargetId_customRequiredAction_success() {
        KeycloakEventNotification event = KeycloakEventNotification.builder()
                .type("CUSTOM_REQUIRED_ACTION")
                .userId("user-456")
                .details(Map.of("custom_required_action", "VERIFY_EMAIL"))
                .build();

        Optional<String> result = KeycloakEventMapper.extractEmailVerificationTargetId(event);

        assertTrue(result.isPresent());
        assertEquals("user-456", result.get());
    }

    @Test
    @DisplayName("should fallback to details.username when userId is null")
    void extractTargetId_fallbackToUsername_success() {
        KeycloakEventNotification event = KeycloakEventNotification.builder()
                .type("CUSTOM_REQUIRED_ACTION")
                .userId(null)
                .details(Map.of(
                        "custom_required_action", "VERIFY_EMAIL",
                        "username", "test.user@example.com"
                ))
                .build();

        Optional<String> result = KeycloakEventMapper.extractEmailVerificationTargetId(event);

        assertTrue(result.isPresent());
        assertEquals("test.user@example.com", result.get());
    }

    @Test
    @DisplayName("should return empty when event type is not verify email")
    void extractTargetId_unrelatedEvent_returnsEmpty() {
        KeycloakEventNotification event = KeycloakEventNotification.builder()
                .type("LOGIN")
                .userId("user-123")
                .build();

        Optional<String> result = KeycloakEventMapper.extractEmailVerificationTargetId(event);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty when event is null or type is null")
    void extractTargetId_nullEventOrType_returnsEmpty() {
        assertTrue(KeycloakEventMapper.extractEmailVerificationTargetId(null).isEmpty());

        KeycloakEventNotification event = KeycloakEventNotification.builder()
                .type(null)
                .userId("user-123")
                .build();
        assertTrue(KeycloakEventMapper.extractEmailVerificationTargetId(event).isEmpty());
    }

    @Test
    @DisplayName("should return empty when target identifier is blank or literal null string")
    void extractTargetId_blankOrNullString_returnsEmpty() {
        KeycloakEventNotification blankEvent = KeycloakEventNotification.builder()
                .type("VERIFY_EMAIL")
                .userId("   ")
                .build();
        assertTrue(KeycloakEventMapper.extractEmailVerificationTargetId(blankEvent).isEmpty());

        KeycloakEventNotification nullStringEvent = KeycloakEventNotification.builder()
                .type("VERIFY_EMAIL")
                .userId("null")
                .build();
        assertTrue(KeycloakEventMapper.extractEmailVerificationTargetId(nullStringEvent).isEmpty());
    }
}
