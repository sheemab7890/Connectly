package com.sheemab.linkedin.connection_service.Services;

import com.sheemab.linkedin.connection_service.Entities.Person;
import com.sheemab.linkedin.connection_service.Repository.PersonRepository;
import com.sheemab.linkedin.connection_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.AcceptConnectionRequestEvent;
import org.example.events.SendConnectionRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {

    private final PersonRepository personRepository;
    private final KafkaTemplate<Long , Object> sendRequestKafkaTemplate;
    private final KafkaTemplate<Long , Object> acceptRequestKafkaTemplate;

    public List<Person> getFirstDegreeConnections() {

        Long userId = SecurityUtils.getCurrentUserId();

        log.info("[getFirstDegreeConnections] Request received");

        if (userId == null) {
            log.error("[getFirstDegreeConnections] User ID is null in context");
            throw new IllegalStateException("User ID cannot be null.");
        }

        log.info("[getFirstDegreeConnections] Fetching first degree connections for userId={}", userId);

        List<Person> connections = personRepository.getFirstDegreeConnections(userId);

        log.info("[getFirstDegreeConnections] Total connections found={}, userId={}",
                connections.size(), userId);

        return connections;
    }


    public Boolean sendConnectionRequest(Long receiverId) {

        Long senderId = SecurityUtils.getCurrentUserId();

        log.info("[sendConnectionRequest] Request received, senderId={}, receiverId={}",
                senderId, receiverId);

        if (senderId == null) {
            log.error("[sendConnectionRequest] Sender id is null");
            throw new IllegalStateException("Sender id is null");
        }

        if (receiverId == null) {
            log.error("[sendConnectionRequest] Receiver id is null, senderId={}", senderId);
            throw new IllegalArgumentException("Receiver id is null");
        }

        if (senderId.equals(receiverId)) {
            log.warn("[sendConnectionRequest] Sender and receiver are same, userId={}", senderId);
            throw new RuntimeException("Both sender and receiver are not same");
        }

        boolean alreadySendRequest =
                personRepository.connectionRequestExists(senderId, receiverId);

        log.debug("[sendConnectionRequest] connectionRequestExists={}, senderId={}, receiverId={}",
                alreadySendRequest, senderId, receiverId);

        if (alreadySendRequest) {
            log.warn("[sendConnectionRequest] Connection request already exists, senderId={}, receiverId={}",
                    senderId, receiverId);
            throw new RuntimeException("Connection request already exist");
        }

        boolean alreadyConnected =
                personRepository.alreadyConnected(senderId, receiverId);

        log.debug("[sendConnectionRequest] alreadyConnected={}, senderId={}, receiverId={}",
                alreadyConnected, senderId, receiverId);

        //  BUG FIX
        // You were checking alreadySendRequest again here
        if (alreadyConnected) {
            log.warn("[sendConnectionRequest] Users are already connected, senderId={}, receiverId={}",
                    senderId, receiverId);
            throw new RuntimeException("Already connected user!");
        }

        log.info("[sendConnectionRequest] Creating connection request in DB, senderId={}, receiverId={}",
                senderId, receiverId);

        personRepository.addConnectionRequest(senderId, receiverId);

        log.info("[sendConnectionRequest] Connection request stored successfully, senderId={}, receiverId={}",
                senderId, receiverId);

        SendConnectionRequestEvent sendConnectionRequestEvent =
                SendConnectionRequestEvent.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .build();

        sendRequestKafkaTemplate.send(
                "send-connection-request-topic",
                sendConnectionRequestEvent
        );

        log.info("[sendConnectionRequest] Kafka event published to send-connection-request-topic, senderId={}, receiverId={}",
                senderId, receiverId);

        return true;
    }


    public Boolean acceptConnectionRequest(Long senderId) {

        Long receiverId = SecurityUtils.getCurrentUserId();

        log.info("[acceptConnectionRequest] Request received, senderId={}, receiverId={}",
                senderId, receiverId);

        if (receiverId == null) {
            log.error("[acceptConnectionRequest] Receiver id is null");
            throw new IllegalStateException("Receiver id is null");
        }

        boolean connectionRequestExists =
                personRepository.connectionRequestExists(senderId, receiverId);

        log.debug("[acceptConnectionRequest] connectionRequestExists={}, senderId={}, receiverId={}",
                connectionRequestExists, senderId, receiverId);

        if (!connectionRequestExists) {
            log.warn("[acceptConnectionRequest] No connection request found, senderId={}, receiverId={}",
                    senderId, receiverId);
            throw new RuntimeException("No connection request exist to accept");
        }

        log.info("[acceptConnectionRequest] Accepting connection request, senderId={}, receiverId={}",
                senderId, receiverId);

        personRepository.acceptConnectionRequest(senderId, receiverId);

        log.info("[acceptConnectionRequest] Connection accepted successfully, senderId={}, receiverId={}",
                senderId, receiverId);

        AcceptConnectionRequestEvent acceptConnectionRequestEvent =
                AcceptConnectionRequestEvent.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .build();

        acceptRequestKafkaTemplate.send(
                "accept-connection-request-topic",
                acceptConnectionRequestEvent
        );

        log.info("[acceptConnectionRequest] Kafka event published to accept-connection-request-topic, senderId={}, receiverId={}",
                senderId, receiverId);

        return true;
    }


    public Boolean rejectConnectionRequest(Long senderId) {

        Long receiverId = SecurityUtils.getCurrentUserId();

        log.info("[rejectConnectionRequest] Request received, senderId={}, receiverId={}",
                senderId, receiverId);

        if (receiverId == null) {
            log.error("[rejectConnectionRequest] Receiver id is null");
            throw new IllegalStateException("Receiver id is null");
        }

        boolean connectionExist =
                personRepository.connectionRequestExists(senderId, receiverId);

        log.debug("[rejectConnectionRequest] connectionRequestExists={}, senderId={}, receiverId={}",
                connectionExist, senderId, receiverId);

        if (!connectionExist) {
            log.warn("[rejectConnectionRequest] No connection request found to reject, senderId={}, receiverId={}",
                    senderId, receiverId);
            throw new RuntimeException("No connection request exist,cannot delete");
        }

        log.info("[rejectConnectionRequest] Rejecting connection request, senderId={}, receiverId={}",
                senderId, receiverId);

        personRepository.rejectConnectionRequest(senderId, receiverId);

        log.info("[rejectConnectionRequest] Connection request rejected successfully, senderId={}, receiverId={}",
                senderId, receiverId);

        return true;
    }

}
