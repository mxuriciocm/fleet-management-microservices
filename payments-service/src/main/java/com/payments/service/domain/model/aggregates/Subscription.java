package com.payments.service.domain.model.aggregates;

import com.payments.service.domain.model.valueobjects.SubscriptionPlan;
import com.payments.service.domain.model.valueobjects.SubscriptionStatus;
import com.payments.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor
public class Subscription extends AuditableAbstractAggregateRoot<Subscription> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "paypal_subscription_id", unique = true)
    private String paypalSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "billing_cycle_anchor")
    private LocalDateTime billingCycleAnchor;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "next_billing_time")
    private LocalDateTime nextBillingTime;

    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    public Subscription(Long userId, SubscriptionPlan plan, BigDecimal amount, String currency) {
        this.userId = userId;
        this.plan = plan;
        this.amount = amount;
        this.currency = currency;
        this.status = SubscriptionStatus.PENDING;
        this.failureCount = 0;
    }

    public void activate(String paypalSubscriptionId, LocalDateTime nextBillingTime) {
        this.paypalSubscriptionId = paypalSubscriptionId;
        this.status = SubscriptionStatus.ACTIVE;
        this.nextBillingTime = nextBillingTime;
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = calculatePeriodEnd();
    }

    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void markPaymentFailed() {
        this.failureCount++;
        if (this.failureCount >= 3) {
            this.status = SubscriptionStatus.SUSPENDED;
        }
    }

    public void resetFailureCount() {
        this.failureCount = 0;
    }

    public void updateBillingCycle(LocalDateTime nextBillingTime) {
        this.nextBillingTime = nextBillingTime;
        this.currentPeriodStart = this.currentPeriodEnd;
        this.currentPeriodEnd = calculatePeriodEnd();
    }

    private LocalDateTime calculatePeriodEnd() {
        return switch (this.plan) {
            case BASIC_MONTHLY, PREMIUM_MONTHLY -> this.currentPeriodStart.plusMonths(1);
            case BASIC_YEARLY, PREMIUM_YEARLY -> this.currentPeriodStart.plusYears(1);
        };
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    public boolean canBeReactivated() {
        return this.status == SubscriptionStatus.SUSPENDED || this.status == SubscriptionStatus.CANCELED;
    }
}
