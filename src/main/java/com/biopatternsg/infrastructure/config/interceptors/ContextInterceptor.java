package com.biopatternsg.infrastructure.config.interceptors;

import com.biopatternsg.domain.exceptions.UnauthorizedServiceException;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Provider
@RequiredArgsConstructor
public class ContextInterceptor implements ContainerRequestFilter {

    private final SessionUtil sessionUtil;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        var context = containerRequestContext.getHeaders();
        validateContext(context);

        sessionUtil.setContext(context);
    }

    private void validateContext(MultivaluedMap<String, String> context){

        var userId = context.get("x-user-id");
        if(userId == null){
            throw new UnauthorizedServiceException();
        }
    }
}
