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
