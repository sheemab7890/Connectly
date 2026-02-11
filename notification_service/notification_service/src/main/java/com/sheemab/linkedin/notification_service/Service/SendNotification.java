package com.sheemab.linkedin.notification_service.Service;

import com.sheemab.linkedin.notification_service.Entity.Notification;
import com.sheemab.linkedin.notification_service.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotification {
    private final NotificationRepository notificationRepository;

    public void sendNotification(Long userId ,String message){
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(userId);
       //userId -> Kisko send krna chahte ho
       //message -> Kya message send krna chahte ho userId ko
        notificationRepository.save(notification);
    }
}
