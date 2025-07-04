package com.payments.service.infrastructure.persistence.jpa.repositories;

import com.payments.service.domain.model.aggregates.Subscription;
import com.payments.service.domain.model.valueobjects.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    List<Subscription> findByStatus(SubscriptionStatus status);
    Optional<Subscription> findByPaypalSubscriptionId(String paypalSubscriptionId);
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
