package com.sheemab.linkedin.user_service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheemab.linkedin.user_service.Entities.OutboxEvent;
import com.sheemab.linkedin.user_service.Repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.UserCreatedEvents;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;


    @Transactional
    @Scheduled(fixedDelay = 10000)
    public void publish() {

        log.debug("Outbox publisher triggered");

        List<OutboxEvent> events =
                outboxRepo.findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        if (events.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        log.info("Fetched {} pending outbox events", events.size());

        for (OutboxEvent e : events) {

            log.debug("Processing outbox event. outboxId={}, eventType={}, aggregateId={}",
                    e.getId(), e.getEventType(), e.getAggregateId());

            try {

                if (!"UserCreatedEvent".equals(e.getEventType())) {

                    log.debug("Skipping unsupported event type. outboxId={}, eventType={}",
                            e.getId(), e.getEventType());

                    continue;
                }

                UserCreatedEvents event =
                        objectMapper.readValue(
                                e.getPayload(),
                                UserCreatedEvents.class
                        );

                log.info("Publishing UserCreatedEvent to Kafka. outboxId={}, userId={}",
                        e.getId(), e.getAggregateId());

                // WAIT for Kafka broker acknowledgement
                kafkaTemplate
                        .send("user_created", e.getAggregateId(), event)
                        .get();

                e.setStatus("SENT");

                log.info("Outbox event published successfully. outboxId={}, topic=user_created",
                        e.getId());

            } catch (Exception ex) {

                log.error("Failed to publish outbox event. outboxId={}, eventType={}. Will retry.",
                        e.getId(), e.getEventType(), ex);

                // leave status as PENDING
            }
        }
    }

}
