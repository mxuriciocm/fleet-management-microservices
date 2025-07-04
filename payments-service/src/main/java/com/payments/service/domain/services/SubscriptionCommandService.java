package com.payments.service.domain.services;

import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.domain.model.commands.CancelSubscriptionCommand;
import com.payments.service.domain.model.commands.CreateSubscriptionCommand;

import java.util.Optional;

public interface SubscriptionCommandService {
    Optional<Subscription> handle(CreateSubscriptionCommand command);
    Optional<Subscription> handle(CancelSubscriptionCommand command);
    void activateSubscription(Long subscriptionId, String paypalSubscriptionId);
    void suspendSubscription(Long subscriptionId);
    void handlePaymentFailure(Long subscriptionId);
    void handlePaymentSuccess(Long subscriptionId);
}
