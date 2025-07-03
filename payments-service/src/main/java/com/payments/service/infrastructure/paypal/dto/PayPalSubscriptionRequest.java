package com.payments.service.infrastructure.paypal.dto;

import java.math.BigDecimal;

public record PayPalSubscriptionRequest(
        String planId,
        BigDecimal amount,
        String currency,
        String returnUrl,
        String cancelUrl,
        Long userId
) {}
