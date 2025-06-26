package com.shipments.service.interfaces.rest.resources;

import com.shipments.service.domain.model.valueobjects.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResource(
    Long id,
    String destination,
    String description,
    ShipmentStatus status,
    LocalDateTime scheduledDate,
    LocalDateTime startedDate,
    LocalDateTime completedDate,
    Long managerId,
    Long carrierId,
    String customerName,
    String customerPhone
) {}
