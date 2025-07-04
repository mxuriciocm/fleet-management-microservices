package com.payments.service.interfaces.rest.transform;

import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.interfaces.rest.resources.SubscriptionResource;

public class SubscriptionResourceFromEntityAssembler {
    public static SubscriptionResource toResourceFromEntity(Subscription entity) {
        return new SubscriptionResource(
                entity.getId(),
                entity.getUserId(),
                entity.getPaypalSubscriptionId(),
                entity.getPlan(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getBillingCycleAnchor(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getNextBillingTime(),
                entity.getTrialEnd(),
                entity.getCanceledAt(),
                entity.getFailureCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
