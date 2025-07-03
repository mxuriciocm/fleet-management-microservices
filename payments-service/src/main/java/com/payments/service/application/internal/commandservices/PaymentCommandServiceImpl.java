package com.payments.service.application.internal.commandservices;

import com.payments.service.application.events.EventsPublisher;
import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.domain.model.commands.CreatePaymentCommand;
import com.payments.service.domain.model.commands.ProcessPaymentCommand;
import com.payments.service.domain.model.events.PaymentCompletedEvent;
import com.payments.service.domain.model.events.PaymentFailedEvent;
import com.payments.service.domain.services.PaymentCommandService;
import com.payments.service.infrastructure.persistence.jpa.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final EventsPublisher eventsPublisher;

    @Override
    @Transactional
    public Optional<Payment> handle(CreatePaymentCommand command) {
        try {
            var payment = new Payment(
                    command.userId(),
                    command.subscriptionId(),
                    command.type(),
                    command.method(),
                    command.amount(),
                    command.currency(),
                    command.description()
            );

            var savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {}", savedPayment.getId());
            return Optional.of(savedPayment);

        } catch (Exception e) {
            log.error("Error creating payment for user {}: {}", command.userId(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Payment> handle(ProcessPaymentCommand command) {
        return paymentRepository.findById(command.paymentId())
                .map(payment -> {
                    payment.markAsProcessing(command.paypalOrderId());
                    var savedPayment = paymentRepository.save(payment);
                    log.info("Payment {} marked as processing", savedPayment.getId());
                    return savedPayment;
                });
    }

    @Override
    @Transactional
    public void completePayment(Long paymentId, String paypalPaymentId) {
        paymentRepository.findById(paymentId)
                .ifPresent(payment -> {
                    payment.markAsCompleted(paypalPaymentId);
                    var savedPayment = paymentRepository.save(payment);

                    var event = new PaymentCompletedEvent(
                            savedPayment.getId(),
                            savedPayment.getUserId(),
                            savedPayment.getSubscriptionId(),
                            savedPayment.getPaypalPaymentId(),
                            savedPayment.getType(),
                            savedPayment.getMethod(),
                            savedPayment.getAmount(),
                            savedPayment.getCurrency(),
                            savedPayment.getProcessedAt()
                    );

                    eventsPublisher.publishPaymentCompletedEvent(event);
                    log.info("Payment {} completed", paymentId);
                });
    }

    @Override
    @Transactional
    public void failPayment(Long paymentId, String failureReason) {
        paymentRepository.findById(paymentId)
                .ifPresent(payment -> {
                    payment.markAsFailed(failureReason);
                    var savedPayment = paymentRepository.save(payment);

                    var event = new PaymentFailedEvent(
                            savedPayment.getId(),
                            savedPayment.getUserId(),
                            savedPayment.getSubscriptionId(),
                            savedPayment.getAmount(),
                            savedPayment.getCurrency(),
                            savedPayment.getFailureReason(),
                            savedPayment.getFailedAt()
                    );

                    eventsPublisher.publishPaymentFailedEvent(event);
                    log.info("Payment {} failed: {}", paymentId, failureReason);
                });
    }

    @Override
    @Transactional
    public void refundPayment(Long paymentId, BigDecimal refundAmount) {
        paymentRepository.findById(paymentId)
                .ifPresent(payment -> {
                    if (payment.canBeRefunded()) {
                        payment.markAsRefunded(refundAmount);
                        paymentRepository.save(payment);
                        log.info("Payment {} refunded with amount {}", paymentId, refundAmount);
                    } else {
                        log.warn("Payment {} cannot be refunded", paymentId);
                    }
                });
    }
}
