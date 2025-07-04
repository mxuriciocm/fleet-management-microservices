package com.payments.service.interfaces.rest.resources;

import com.payments.service.domain.model.valueobjects.PaymentMethod;
import com.payments.service.domain.model.valueobjects.PaymentType;

import java.math.BigDecimal;

public record CreatePaymentResource(
        Long userId,
        Long subscriptionId,
        PaymentType type,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String description,
        String returnUrl,
        String cancelUrl
) {}
