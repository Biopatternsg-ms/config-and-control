package com.biopatternsg.infrastructure.dtos;

public record LoginRequest(
        String user,
        String pass
) {
}
