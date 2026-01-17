package com.biopatternsg.domain.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ExceptionService extends RuntimeException{

    private final transient HttpCode httpCode;
    private final transient Map<String, Object> details;
}
