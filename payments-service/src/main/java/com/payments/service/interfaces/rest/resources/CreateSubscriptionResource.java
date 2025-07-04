package com.payments.service.interfaces.rest.resources;

import com.payments.service.domain.model.valueobjects.SubscriptionPlan;

public record CreateSubscriptionResource(
        Long userId,
        SubscriptionPlan plan,
        String returnUrl,
        String cancelUrl
) {}
