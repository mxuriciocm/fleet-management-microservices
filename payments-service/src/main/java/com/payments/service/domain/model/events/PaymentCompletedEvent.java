package com.payments.service.domain.model.events;

import com.payments.service.domain.model.valueobjects.PaymentMethod;
import com.payments.service.domain.model.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        Long paymentId,
        Long userId,
        Long subscriptionId,
        String paypalPaymentId,
        PaymentType type,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        LocalDateTime processedAt
) {}
