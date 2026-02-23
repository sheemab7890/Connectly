package com.sheemab.linkedin.post_service.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * FeignAuthForwardInterceptor
 *
 * This interceptor automatically forwards the incoming HTTP
 * Authorization header to all outgoing Feign client requests.
 *
 * Purpose:
 * In a microservices architecture, when one service calls another
 * using Feign, the original JWT token is NOT automatically propagated.
 *
 * This interceptor ensures token propagation so that downstream
 * services can authenticate the same user.
 */
@Component
public class FeignAuthForwardInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();  // So if token is not stored → Feign cannot forward it.
            template.header("Authorization", "Bearer " + token);
        }
    }
}

