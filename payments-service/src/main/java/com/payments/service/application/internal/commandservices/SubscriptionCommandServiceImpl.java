package com.payments.service.application.internal.commandservices;

import com.payments.service.application.events.EventsPublisher;
import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.domain.model.commands.CancelSubscriptionCommand;
import com.payments.service.domain.model.commands.CreateSubscriptionCommand;
import com.payments.service.domain.model.events.SubscriptionActivatedEvent;
import com.payments.service.domain.model.events.SubscriptionCanceledEvent;
import com.payments.service.domain.model.events.SubscriptionCreatedEvent;
import com.payments.service.domain.services.SubscriptionCommandService;
import com.payments.service.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final EventsPublisher eventsPublisher;

    @Override
    @Transactional
    public Optional<Subscription> handle(CreateSubscriptionCommand command) {
        try {
            var subscription = new Subscription(
                    command.userId(),
                    command.plan(),
                    command.plan().getPrice(),
                    command.plan().getCurrency()
            );

            var savedSubscription = subscriptionRepository.save(subscription);

            var event = new SubscriptionCreatedEvent(
                    savedSubscription.getId(),
                    savedSubscription.getUserId(),
                    savedSubscription.getPaypalSubscriptionId(),
                    savedSubscription.getPlan(),
                    savedSubscription.getStatus(),
                    savedSubscription.getAmount(),
                    savedSubscription.getCurrency(),
                    savedSubscription.getCreatedAt()
            );

            eventsPublisher.publishSubscriptionCreatedEvent(event);

            log.info("Subscription created with ID: {}", savedSubscription.getId());
            return Optional.of(savedSubscription);

        } catch (Exception e) {
            log.error("Error creating subscription for user {}: {}", command.userId(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(CancelSubscriptionCommand command) {
        return subscriptionRepository.findById(command.subscriptionId())
                .map(subscription -> {
                    subscription.cancel();
                    var savedSubscription = subscriptionRepository.save(subscription);

                    var event = new SubscriptionCanceledEvent(
                            savedSubscription.getId(),
                            savedSubscription.getUserId(),
                            savedSubscription.getPaypalSubscriptionId(),
                            command.reason(),
                            savedSubscription.getCanceledAt()
                    );

                    eventsPublisher.publishSubscriptionCanceledEvent(event);

                    log.info("Subscription {} canceled", savedSubscription.getId());
                    return savedSubscription;
                });
    }

    @Override
    @Transactional
    public void activateSubscription(Long subscriptionId, String paypalSubscriptionId) {
        subscriptionRepository.findById(subscriptionId)
                .ifPresent(subscription -> {
                    var nextBillingTime = LocalDateTime.now().plusMonths(subscription.getPlan().getBillingCycleMonths());
                    subscription.activate(paypalSubscriptionId, nextBillingTime);
                    var savedSubscription = subscriptionRepository.save(subscription);

                    var event = new SubscriptionActivatedEvent(
                            savedSubscription.getId(),
                            savedSubscription.getUserId(),
                            savedSubscription.getPaypalSubscriptionId(),
                            LocalDateTime.now(),
                            savedSubscription.getNextBillingTime()
                    );

                    eventsPublisher.publishSubscriptionActivatedEvent(event);

                    log.info("Subscription {} activated", subscriptionId);
                });
    }

    @Override
    @Transactional
    public void suspendSubscription(Long subscriptionId) {
        subscriptionRepository.findById(subscriptionId)
                .ifPresent(subscription -> {
                    subscription.suspend();
                    subscriptionRepository.save(subscription);
                    log.info("Subscription {} suspended", subscriptionId);
                });
    }

    @Override
    @Transactional
    public void handlePaymentFailure(Long subscriptionId) {
        subscriptionRepository.findById(subscriptionId)
                .ifPresent(subscription -> {
                    subscription.markPaymentFailed();
                    subscriptionRepository.save(subscription);
                    log.info("Payment failure handled for subscription {}", subscriptionId);
                });
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long subscriptionId) {
        subscriptionRepository.findById(subscriptionId)
                .ifPresent(subscription -> {
                    subscription.resetFailureCount();
                    subscriptionRepository.save(subscription);
                    log.info("Payment success handled for subscription {}", subscriptionId);
                });
    }
}
