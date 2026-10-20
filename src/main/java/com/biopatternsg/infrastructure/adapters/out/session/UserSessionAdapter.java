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
package com.biopatternsg.infrastructure.adapters.out.session;

import com.biopatternsg.domain.port.out.UserSessionPort;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserSessionAdapter implements UserSessionPort {

    @Inject
    private SessionUtil sessionUtil;

    @Override
    public void setUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            MultivaluedMap<String, String> context = new MultivaluedHashMap<>();
            context.putSingle("x-user-id", userId);
            sessionUtil.setContext(context);
        }
    }

    @Override
    public String getUserId() {
        return sessionUtil != null ? sessionUtil.getUserId() : null;
    }
}
