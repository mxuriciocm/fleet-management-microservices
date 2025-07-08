package com.payments.service.domain.model.commands;

public record CancelSubscriptionCommand(
        Long subscriptionId,
        String reason
) {}
