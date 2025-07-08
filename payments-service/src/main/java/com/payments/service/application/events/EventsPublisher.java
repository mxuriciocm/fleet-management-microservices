package com.payments.service.application.events;

import com.payments.service.domain.model.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SUBSCRIPTION_CREATED_TOPIC = "subscription.created";
    private static final String SUBSCRIPTION_ACTIVATED_TOPIC = "subscription.activated";
    private static final String SUBSCRIPTION_CANCELED_TOPIC = "subscription.canceled";
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    public void publishSubscriptionCreatedEvent(SubscriptionCreatedEvent event) {
        try {
            kafkaTemplate.send(SUBSCRIPTION_CREATED_TOPIC, event);
            log.info("Published subscription created event for subscription: {}", event.subscriptionId());
        } catch (Exception e) {
            log.error("Error publishing subscription created event: {}", e.getMessage());
        }
    }

    public void publishSubscriptionActivatedEvent(SubscriptionActivatedEvent event) {
        try {
            kafkaTemplate.send(SUBSCRIPTION_ACTIVATED_TOPIC, event);
            log.info("Published subscription activated event for subscription: {}", event.subscriptionId());
        } catch (Exception e) {
            log.error("Error publishing subscription activated event: {}", e.getMessage());
        }
    }

    public void publishSubscriptionCanceledEvent(SubscriptionCanceledEvent event) {
        try {
            kafkaTemplate.send(SUBSCRIPTION_CANCELED_TOPIC, event);
            log.info("Published subscription canceled event for subscription: {}", event.subscriptionId());
        } catch (Exception e) {
            log.error("Error publishing subscription canceled event: {}", e.getMessage());
        }
    }

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event);
            log.info("Published payment completed event for payment: {}", event.paymentId());
        } catch (Exception e) {
            log.error("Error publishing payment completed event: {}", e.getMessage());
        }
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event);
            log.info("Published payment failed event for payment: {}", event.paymentId());
        } catch (Exception e) {
            log.error("Error publishing payment failed event: {}", e.getMessage());
        }
    }
}
