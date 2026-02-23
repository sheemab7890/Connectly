package com.sheemab.linkedin.notification_service.sseEmitter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        log.info("SSE CONNECTED for userId={}", userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE COMPLETED for userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE TIMEOUT for userId={}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId);
            log.error("SSE ERROR for userId={}", userId);
        });

        return emitter;
    }

    public void sendToUser(Long userId, String message) {

        log.info("Trying to send SSE to userId={}", userId);

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.warn("No active SSE connection for userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(message));

            log.info("SSE SENT to userId={}", userId);

        } catch (IOException e) {
            emitters.remove(userId);
            log.error("SSE FAILED for userId={}", userId);
        }
    }
}