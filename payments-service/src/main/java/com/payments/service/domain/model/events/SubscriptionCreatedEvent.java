package com.payments.service.domain.model.events;

import com.payments.service.domain.model.valueobjects.SubscriptionPlan;
import com.payments.service.domain.model.valueobjects.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionCreatedEvent(
        Long subscriptionId,
        Long userId,
        String paypalSubscriptionId,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt
) {}
