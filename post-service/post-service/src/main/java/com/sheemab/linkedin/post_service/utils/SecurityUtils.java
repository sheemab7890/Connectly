package com.sheemab.linkedin.post_service.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String) {
            return Long.parseLong((String) auth.getPrincipal());
        }
        throw new RuntimeException("No authenticated user found");
    }

}
