package com.shipments.service.domain.model.events;

import com.shipments.service.domain.model.valueobjects.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Event published when a shipment is updated
 * @param shipmentId The ID of the updated shipment
 * @param destination The destination of the shipment (null if not changed)
 * @param description The description of the shipment (null if not changed)
 * @param status The status of the shipment (null if not changed)
 * @param scheduledDate The scheduled date of the shipment (null if not changed)
 * @param carrierId The ID of the carrier assigned to the shipment (null if not assigned/changed)
 */
public record ShipmentUpdatedEvent(
    Long shipmentId,
    String destination,
    String description,
    ShipmentStatus status,
    LocalDateTime scheduledDate,
    Long carrierId
) {}
