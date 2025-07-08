package com.payments.service.domain.model.commands;

public record ProcessPaymentCommand(
        Long paymentId,
        String paypalOrderId
) {}
