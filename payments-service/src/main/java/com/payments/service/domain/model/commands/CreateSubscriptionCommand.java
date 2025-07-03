package com.payments.service.domain.model.commands;

import com.payments.service.domain.model.valueobjects.SubscriptionPlan;

public record CreateSubscriptionCommand(
        Long userId,
        SubscriptionPlan plan,
        String returnUrl,
        String cancelUrl
) {}
