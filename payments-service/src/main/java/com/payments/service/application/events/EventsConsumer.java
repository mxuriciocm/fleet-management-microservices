package com.payments.service.application.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsConsumer {

    @KafkaListener(topics = "user.created", groupId = "payments-service")
    public void handleUserCreatedEvent(com.payments.service.application.events.UserCreatedEvent event) {
        log.info("Received user created event for user: {}", event.userId());
    }
}
