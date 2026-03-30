package com.biopatternsg.infrastructure.dtos;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersKeycloakFiltersRequest{

    @QueryParam("username")
    String username;
    @QueryParam("email")
    String email;
    @QueryParam("search")
    String search;
    @QueryParam("enabled")
    Boolean enable;
}
