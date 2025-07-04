package com.payments.service.infrastructure.persistence.jpa.repositories;


import com.payments.service.domain.model.aggregates.Payment;
import com.payments.service.domain.model.valueobjects.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findBySubscriptionId(Long subscriptionId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByPaypalPaymentId(String paypalPaymentId);
    Optional<Payment> findByPaypalOrderId(String paypalOrderId);
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
