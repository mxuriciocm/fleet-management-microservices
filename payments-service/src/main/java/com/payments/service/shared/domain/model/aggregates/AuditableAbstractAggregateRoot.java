package com.payments.service.shared.domain.model.aggregates;

import com.payments.service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class AuditableAbstractAggregateRoot<T extends AuditableAbstractAggregateRoot<T>> extends AuditableModel {
}
