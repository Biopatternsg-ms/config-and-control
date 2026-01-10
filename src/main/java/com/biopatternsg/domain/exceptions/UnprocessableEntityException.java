package com.biopatternsg.domain.exceptions;

import java.util.Map;

public class UnprocessableEntityException extends ExceptionService{

    public UnprocessableEntityException(Map<String, Object> details) {
        super(HttpCode.UNPROCESSABLE_ENTITY, details);
    }

    public UnprocessableEntityException(String message) {
        super(HttpCode.UNPROCESSABLE_ENTITY, Map.of("message", message));
    }
}
