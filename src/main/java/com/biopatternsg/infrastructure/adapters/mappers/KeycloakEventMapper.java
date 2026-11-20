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
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.infrastructure.dtos.keycloak.KeycloakEventNotification;

import java.util.Optional;

public final class KeycloakEventMapper {

    private static final String VERIFY_EMAIL_TYPE = "VERIFY_EMAIL";
    private static final String CUSTOM_REQUIRED_ACTION_TYPE = "CUSTOM_REQUIRED_ACTION";
    private static final String CUSTOM_REQUIRED_ACTION_DETAIL = "custom_required_action";
    private static final String USERNAME_DETAIL = "username";

    private KeycloakEventMapper() {
    }

    public static Optional<String> extractEmailVerificationTargetId(KeycloakEventNotification event) {
        if (event == null || event.type() == null) {
            return Optional.empty();
        }

        boolean isVerifyEmail = VERIFY_EMAIL_TYPE.equalsIgnoreCase(event.type()) ||
                (CUSTOM_REQUIRED_ACTION_TYPE.equalsIgnoreCase(event.type())
                        && event.details() != null
                        && VERIFY_EMAIL_TYPE.equalsIgnoreCase(String.valueOf(event.details().get(CUSTOM_REQUIRED_ACTION_DETAIL))));

        if (!isVerifyEmail) {
            return Optional.empty();
        }

        String targetId = (event.userId() != null && !event.userId().isBlank())
                ? event.userId().trim()
                : (event.details() != null ? String.valueOf(event.details().get(USERNAME_DETAIL)).trim() : null);

        if (targetId != null && !targetId.isBlank() && !"null".equalsIgnoreCase(targetId)) {
            return Optional.of(targetId);
        }

        return Optional.empty();
    }
}
