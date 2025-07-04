package com.payments.service.domain.services;

import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.domain.model.commands.CreatePaymentCommand;
import com.payments.service.domain.model.commands.ProcessPaymentCommand;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentCommandService {
    Optional<Payment> handle(CreatePaymentCommand command);
    Optional<Payment> handle(ProcessPaymentCommand command);
    void completePayment(Long paymentId, String paypalPaymentId);
    void failPayment(Long paymentId, String failureReason);
    void refundPayment(Long paymentId, BigDecimal refundAmount);
}