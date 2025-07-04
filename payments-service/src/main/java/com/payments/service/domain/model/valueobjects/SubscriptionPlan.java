package com.payments.service.domain.model.valueobjects;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum SubscriptionPlan {
    BASIC_MONTHLY("basic-monthly", "Basic Monthly Plan", new BigDecimal("29.99"), "USD", 1),
    BASIC_YEARLY("basic-yearly", "Basic Yearly Plan", new BigDecimal("299.99"), "USD", 12),
    PREMIUM_MONTHLY("premium-monthly", "Premium Monthly Plan", new BigDecimal("59.99"), "USD", 1),
    PREMIUM_YEARLY("premium-yearly", "Premium Yearly Plan", new BigDecimal("599.99"), "USD", 12);

    private final String planId;
    private final String displayName;
    private final BigDecimal price;
    private final String currency;
    private final int billingCycleMonths;

    SubscriptionPlan(String planId, String displayName, BigDecimal price, String currency, int billingCycleMonths) {
        this.planId = planId;
        this.displayName = displayName;
        this.price = price;
        this.currency = currency;
        this.billingCycleMonths = billingCycleMonths;
    }

    public static SubscriptionPlan fromPlanId(String planId) {
        for (SubscriptionPlan plan : values()) {
            if (plan.planId.equals(planId)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unknown subscription plan: " + planId);
    }
}
