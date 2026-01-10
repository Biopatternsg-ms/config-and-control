package com.biopatternsg.domain.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum HttpCode {

    INTERNAL_SERVER_ERROR(500),
    UNPROCESSABLE_ENTITY(422),
    UNAUTHORIZED(401);

    private final Integer code;
}
