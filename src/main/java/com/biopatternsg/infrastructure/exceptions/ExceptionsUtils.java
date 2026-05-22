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
package com.biopatternsg.infrastructure.exceptions;

import com.biopatternsg.domain.exceptions.ExceptionService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@UtilityClass
public class ExceptionsUtils {

    private static final String MESSAGE = "Oops, something unexpected happens";

    public static Response toResponse(ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);
        Map<String, String> details = exception.getConstraintViolations().stream()
                .collect(toMap(
                        ExceptionsUtils::getFieldName,
                        ConstraintViolation::getMessage
                ));

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(details)
                .build();
    }

    public static Response toResponse(Throwable exception) {
        log.error(exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", MESSAGE))
                .build();
    }

    public static Response toResponse(ExceptionService exception) {
        log.error(exception.getMessage(), exception);
        return Response.status(exception.getHttpCode().getCode())
                .entity(exception.getDetails())
                .build();
    }

    private static String getFieldName(ConstraintViolation<?> violation) {
        String[] path = violation.getPropertyPath().toString().split("\\.");
        return path.length > 0 ? path[path.length - 1] : "unknown";
    }
}
