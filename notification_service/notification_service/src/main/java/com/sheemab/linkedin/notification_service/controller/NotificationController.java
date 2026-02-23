package com.sheemab.linkedin.notification_service.controller;


import com.sheemab.linkedin.notification_service.sseEmitter.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterService sseEmitterService;

    @GetMapping("/stream")
    public SseEmitter stream() {

        // userId extracted from JWT via SecurityContext
        String userIdStr = (String) org.springframework.security.core.context
                .SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = Long.valueOf(userIdStr);

        return sseEmitterService.createEmitter(userId);
    }
}
