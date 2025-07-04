package com.payments.service.interfaces.rest.transform;

import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {
    public static PaymentResource toResourceFromEntity(Payment entity) {
        return new PaymentResource(
                entity.getId(),
                entity.getUserId(),
                entity.getSubscriptionId(),
                entity.getPaypalPaymentId(),
                entity.getPaypalOrderId(),
                entity.getType(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getDescription(),
                entity.getProcessedAt(),
                entity.getFailedAt(),
                entity.getFailureReason(),
                entity.getRefundedAt(),
                entity.getRefundAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
