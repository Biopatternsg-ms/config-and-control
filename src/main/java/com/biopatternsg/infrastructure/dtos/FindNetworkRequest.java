package com.biopatternsg.infrastructure.dtos;

import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public record FindNetworkRequest(

        @QueryParam("id")
        String id,
        @QueryParam("name")
        String name,
        @QueryParam("description")
        String description,
        @QueryParam("page")
        @DefaultValue("0")
        @Min(0)
        int page,
        @QueryParam("size")
        @DefaultValue("10")
        int size
) {
}
