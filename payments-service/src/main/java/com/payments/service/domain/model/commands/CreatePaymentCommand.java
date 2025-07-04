package com.payments.service.domain.model.commands;

import com.payments.service.domain.model.valueobjects.PaymentMethod;
import com.payments.service.domain.model.valueobjects.PaymentType;

import java.math.BigDecimal;

public record CreatePaymentCommand(
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
