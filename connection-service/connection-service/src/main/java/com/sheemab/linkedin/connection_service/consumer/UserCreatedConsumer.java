package com.sheemab.linkedin.connection_service.consumer;

import com.sheemab.linkedin.connection_service.Entities.Person;
import com.sheemab.linkedin.connection_service.Repository.PersonRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.events.UserCreatedEvents;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedConsumer {

    private final PersonRepository personRepository;

    @KafkaListener(
            topics = "user_created",
            groupId = "connection-service"
    )
    public void consume(UserCreatedEvents event) {

        log.info("Received UserCreatedEvent. userId={}, name={}, email={}",
                event.getUserId(), event.getName(), event.getEmail());

        try {

            // idempotency check
            boolean alreadyExists = personRepository.existsByUserId(event.getUserId());

            if (alreadyExists) {
                log.warn("Person node already exists for userId={}, skipping creation", event.getUserId());
                return;
            }

            Person node = new Person();
            node.setUserId(event.getUserId());
            node.setName(event.getName());
            node.setEmail(event.getEmail());

            personRepository.save(node);

            log.info("Person node created successfully in Neo4j. userId={}", event.getUserId());

        } catch (Exception ex) {

            log.error("Failed to process UserCreatedEvent for userId={}",
                    event.getUserId(), ex);

            // rethrow so Kafka error handler can handle retry / seek
            throw ex;
        }
    }
}


