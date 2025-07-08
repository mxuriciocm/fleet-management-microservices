package com.payments.service.infrastructure.paypal.dto;

public record PayPalSubscriptionResponse(
        String subscriptionId,
        String approvalUrl,
        String status
) {}
