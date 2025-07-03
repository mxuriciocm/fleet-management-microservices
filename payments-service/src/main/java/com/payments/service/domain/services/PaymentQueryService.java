package com.payments.service.domain.services;

import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.domain.model.queries.GetPaymentByIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsBySubscriptionIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface PaymentQueryService {
    Optional<Payment> handle(GetPaymentByIdQuery query);
    List<Payment> handle(GetPaymentsByUserIdQuery query);
    List<Payment> handle(GetPaymentsBySubscriptionIdQuery query);
}