package com.payments.service.interfaces.rest.resources;

import com.payments.service.domain.model.valueobjects.SubscriptionPlan;
import com.payments.service.domain.model.valueobjects.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResource(
        Long id,
        Long userId,
        String paypalSubscriptionId,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime billingCycleAnchor,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime nextBillingTime,
        LocalDateTime trialEnd,
        LocalDateTime canceledAt,
        Integer failureCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
