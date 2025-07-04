package com.payments.service.infrastructure.paypal.dto;

public record PayPalOrderResponse(
        String orderId,
        String approvalUrl,
        String status
) {}
