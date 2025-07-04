package com.payments.service.application.internal.queryservices;

import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.domain.model.queries.GetPaymentByIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsBySubscriptionIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsByUserIdQuery;
import com.payments.service.domain.services.PaymentQueryService;
import com.payments.service.infrastructure.persistence.jpa.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    @Override
    public Optional<Payment> handle(GetPaymentByIdQuery query) {
        return paymentRepository.findById(query.paymentId());
    }

    @Override
    public List<Payment> handle(GetPaymentsByUserIdQuery query) {
        return paymentRepository.findByUserId(query.userId());
    }

    @Override
    public List<Payment> handle(GetPaymentsBySubscriptionIdQuery query) {
        return paymentRepository.findBySubscriptionId(query.subscriptionId());
    }
}
