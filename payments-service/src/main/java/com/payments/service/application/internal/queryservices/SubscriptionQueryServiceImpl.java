package com.payments.service.application.internal.queryservices;

import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.domain.model.queries.GetSubscriptionByIdQuery;
import com.payments.service.domain.model.queries.GetSubscriptionsByUserIdQuery;
import com.payments.service.domain.services.SubscriptionQueryService;
import com.payments.service.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Optional<Subscription> handle(GetSubscriptionByIdQuery query) {
        return subscriptionRepository.findById(query.subscriptionId());
    }

    @Override
    public List<Subscription> handle(GetSubscriptionsByUserIdQuery query) {
        return subscriptionRepository.findByUserId(query.userId());
    }
}
