package com.sheemab.linkedin.notification_service.Service;

import com.sheemab.linkedin.notification_service.Entity.Notification;
import com.sheemab.linkedin.notification_service.Repository.NotificationRepository;
import com.sheemab.linkedin.notification_service.sseEmitter.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotification {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public void sendNotification(Long userId, String message) {

        // 1️⃣ Save in DB
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(userId);

        notificationRepository.save(notification);

        log.info("Notification saved for userId={}", userId);

        // 2️⃣ Push real-time via SSE
        sseEmitterService.sendToUser(userId, message);
    }
}
