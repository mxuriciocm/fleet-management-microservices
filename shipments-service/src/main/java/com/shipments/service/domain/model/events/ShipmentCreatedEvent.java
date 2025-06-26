package com.shipments.service.domain.model.events;

import com.shipments.service.domain.model.valueobjects.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Event published when a shipment is created
 * @param shipmentId The ID of the newly created shipment
 * @param destination The destination of the shipment
 * @param description The description of the shipment
 * @param status The status of the shipment
 * @param scheduledDate The scheduled date of the shipment
 * @param managerId The ID of the manager who created the shipment
 * @param customerName The name of the customer for this shipment
 * @param customerPhone The phone number of the customer for this shipment
 */
public record ShipmentCreatedEvent(
    Long shipmentId,
    String destination,
    String description,
    ShipmentStatus status,
    LocalDateTime scheduledDate,
    Long managerId,
    String customerName,
    String customerPhone
) {}
