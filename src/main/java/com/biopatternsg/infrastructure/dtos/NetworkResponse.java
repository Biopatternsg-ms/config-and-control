package com.biopatternsg.infrastructure.dtos;

import lombok.Builder;

@Builder
public record NetworkResponse(
        String id,
        String userId,
        String name,
        String description,
        int createdAt
) {
}
