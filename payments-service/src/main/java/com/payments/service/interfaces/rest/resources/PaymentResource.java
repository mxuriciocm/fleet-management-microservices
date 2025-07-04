package com.payments.service.interfaces.rest.resources;

import com.payments.service.domain.model.valueobjects.PaymentMethod;
import com.payments.service.domain.model.valueobjects.PaymentStatus;
import com.payments.service.domain.model.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResource(
        Long id,
        Long userId,
        Long subscriptionId,
        String paypalPaymentId,
        String paypalOrderId,
        PaymentType type,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String description,
        LocalDateTime processedAt,
        LocalDateTime failedAt,
        String failureReason,
        LocalDateTime refundedAt,
        BigDecimal refundAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
