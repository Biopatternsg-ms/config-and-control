package com.biopatternsg.infrastructure.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApplicationScoped
public class SessionUtil {

    private MultivaluedMap<String, String> context;

    public Long getUserId(){
        return Long.parseLong(this.context.get("x-user-id").getFirst());
    }
}
