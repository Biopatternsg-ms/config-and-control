package com.biopatternsg.domain.exceptions;

import java.util.Map;

public class UnauthorizedServiceException extends ExceptionService{

    private static final String MESSAGE = "Sorry, unauthorized access";

    public UnauthorizedServiceException() {
        super(HttpCode.UNAUTHORIZED, Map.of("message", MESSAGE));
    }
}
