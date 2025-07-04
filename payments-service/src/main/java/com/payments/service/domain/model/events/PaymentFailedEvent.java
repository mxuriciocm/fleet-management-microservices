package com.payments.service.domain.model.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long paymentId,
        Long userId,
        Long subscriptionId,
        BigDecimal amount,
        String currency,
        String failureReason,
        LocalDateTime failedAt
) {}
