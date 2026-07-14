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
package com.biopatternsg.infrastructure.config.interceptors;

import com.biopatternsg.domain.exceptions.UnauthorizedServiceException;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Provider
@RequiredArgsConstructor
@Slf4j
public class ContextInterceptor implements ContainerRequestFilter {

    private final SessionUtil sessionUtil;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        log.info("Path: {}", path);

        if (path.contains("config-and-control/users")) {
            log.info("userId no needed");
            return;
        }

        var context = requestContext.getHeaders();
        validateUserContext(context);

        if (path.contains("config-and-control/admin")) {
            validateRoleContext(context);
            log.info("userId no needed");
            return;
        }

        sessionUtil.setContext(context);
    }

    private void validateUserContext(MultivaluedMap<String, String> context){

        var userId = context.get("x-user-id");
        // log.info("userId: {}", userId);
        if(userId == null){
            throw new UnauthorizedServiceException();
        }
    }

    private void validateRoleContext(MultivaluedMap<String, String> context){

        var roles = context.get("x-user-roles");
        // log.info("roles: {}", roles);
        if (roles == null || roles.isEmpty()) {
            throw new UnauthorizedServiceException();
        }

        boolean hasAdmin = roles.stream()
                .filter(Objects::nonNull)
                .anyMatch(r -> java.util.Arrays.asList(r.split(",")).contains("admin"));
        if (!hasAdmin) {
            throw new UnauthorizedServiceException();
        }
    }
}
