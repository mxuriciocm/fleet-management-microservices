package com.payments.service.domain.services;


import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.domain.model.queries.GetSubscriptionByIdQuery;
import com.payments.service.domain.model.queries.GetSubscriptionsByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface SubscriptionQueryService {
    Optional<Subscription> handle(GetSubscriptionByIdQuery query);
    List<Subscription> handle(GetSubscriptionsByUserIdQuery query);
}

