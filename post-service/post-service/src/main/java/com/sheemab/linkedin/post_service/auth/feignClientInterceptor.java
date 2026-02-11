package com.sheemab.linkedin.post_service.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class feignClientInterceptor implements RequestInterceptor {
    // This runs whenever your service calls another service using Feign.
    // This is used to pass the userId to downstream services
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = UserContextHolder.getCurrentUserId();

        if(userId != null){
            requestTemplate.header("X-User-Id", String.valueOf(userId));
        }
    }
}
