package com.payments.service.domain.model.events;


import java.time.LocalDateTime;

public record SubscriptionActivatedEvent(
        Long subscriptionId,
        Long userId,
        String paypalSubscriptionId,
        LocalDateTime activatedAt,
        LocalDateTime nextBillingTime
) {}
