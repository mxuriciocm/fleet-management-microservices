package com.payments.service.domain.model.events;

import java.time.LocalDateTime;

public record SubscriptionCanceledEvent(
        Long subscriptionId,
        Long userId,
        String paypalSubscriptionId,
        String reason,
        LocalDateTime canceledAt
) {}
