package com.biopatternsg.infrastructure.config.interceptors;

import com.biopatternsg.domain.exceptions.UnauthorizedServiceException;
import com.biopatternsg.infrastructure.session.SessionUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Provider
@RequiredArgsConstructor
@Slf4j
public class ContextInterceptor implements ContainerRequestFilter {

    private final SessionUtil sessionUtil;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();
        if (path.contains("/auth") || path.contains("/user/recover") || path.contains("/user/register")) {
            return;
        }

        var context = requestContext.getHeaders();
        validateContext(context);

        sessionUtil.setContext(context);
    }

    private void validateContext(MultivaluedMap<String, String> context){

        var userId = context.get("x-user-id");
        log.info("userId: " + userId);
        if(userId == null){
            throw new UnauthorizedServiceException();
        }
    }
}
