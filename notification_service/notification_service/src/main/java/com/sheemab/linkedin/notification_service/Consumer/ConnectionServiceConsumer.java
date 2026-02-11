package com.sheemab.linkedin.notification_service.Consumer;

import com.sheemab.linkedin.connection_service.Event.AcceptConnectionRequestEvent;
import com.sheemab.linkedin.connection_service.Event.SendConnectionRequestEvent;
import com.sheemab.linkedin.notification_service.Service.SendNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionServiceConsumer {
    private final SendNotification notificationService;

    @KafkaListener(topics = "send-connection-request-topic")
    public void handleSendConnectionRequest(SendConnectionRequestEvent sendConnectionRequestEvent){
        String message = String.format("User %d send a connection request to you",
                sendConnectionRequestEvent.getSenderId());
        notificationService.sendNotification(sendConnectionRequestEvent.getReceiverId(), message);
    }

    @KafkaListener(topics = "accept-connection-request-topic")
    public void handleAcceptConnectionRequest(AcceptConnectionRequestEvent acceptConnectionRequestEvent){
        String message = String.format("Your connection request has been accepted by the user %d",
        acceptConnectionRequestEvent.getReceiverId());
        notificationService.sendNotification(acceptConnectionRequestEvent.getSenderId(), message);
    }
}
