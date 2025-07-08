package com.payments.service.domain.model.aggregates;

import com.payments.service.domain.model.valueobjects.PaymentMethod;
import com.payments.service.domain.model.valueobjects.PaymentStatus;
import com.payments.service.domain.model.valueobjects.PaymentType;
import com.payments.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment extends AuditableAbstractAggregateRoot<Payment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "paypal_payment_id", unique = true)
    private String paypalPaymentId;

    @Column(name = "paypal_order_id")
    private String paypalOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "description")
    private String description;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    public Payment(Long userId, PaymentType type, PaymentMethod method,
                   BigDecimal amount, String currency, String description) {
        this.userId = userId;
        this.type = type;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.status = PaymentStatus.PENDING;
    }

    public Payment(Long userId, Long subscriptionId, PaymentType type,
                   PaymentMethod method, BigDecimal amount, String currency, String description) {
        this(userId, type, method, amount, currency, description);
        this.subscriptionId = subscriptionId;
    }

    public void markAsProcessing(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
        this.status = PaymentStatus.PROCESSING;
    }

    public void markAsCompleted(String paypalPaymentId) {
        this.paypalPaymentId = paypalPaymentId;
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = failureReason;
    }

    public void markAsRefunded(BigDecimal refundAmount) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundAmount = refundAmount;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    public boolean canBeRefunded() {
        return this.status == PaymentStatus.COMPLETED && this.refundedAt == null;
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }
}
