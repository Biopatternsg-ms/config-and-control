package com.biopatternsg.domain.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum HttpCode {

    UNAUTHORIZED(401),
    FORBIDDEN(403),
    CONFLICTS(409),
    UNPROCESSABLE_ENTITY(422),
    INTERNAL_SERVER_ERROR(500);

    private final Integer code;

    HttpCode(int code) {
        this.code = code;
    }

    public static HttpCode getValue(int value) {
        for (HttpCode status : HttpCode.values()) {
            if (status.code == value) {
                return status;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}
