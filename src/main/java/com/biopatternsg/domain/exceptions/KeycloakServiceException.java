package com.biopatternsg.domain.exceptions;

import java.util.Map;

public class KeycloakServiceException extends ExceptionService{

    private static final String DEFAULT_MESSAGE = "Error with identity provider.";

    public KeycloakServiceException(Integer httpCode) {
        super(HttpCode.getValue(httpCode), Map.of("message", getFriendlyMessage(httpCode)));
    }

    private static String getFriendlyMessage(Integer code) {
        return switch (code) {
            case 400 -> "Bad request.";
            case 401 -> "Authentication failed with the identity provider.";
            case 403 -> "The application lacks permissions to manage users.";
            case 409 -> "The user or email already exists.";
            default -> DEFAULT_MESSAGE;
        };
    }
}
