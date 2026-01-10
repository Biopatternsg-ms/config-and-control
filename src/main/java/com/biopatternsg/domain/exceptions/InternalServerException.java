package com.biopatternsg.domain.exceptions;

import java.util.Map;

public class InternalServerException extends ExceptionService{

    private static final String MESSAGE = "Oops, something unexpected happens";

    public InternalServerException() {
        super(HttpCode.INTERNAL_SERVER_ERROR, Map.of("message", MESSAGE));

    }
}
