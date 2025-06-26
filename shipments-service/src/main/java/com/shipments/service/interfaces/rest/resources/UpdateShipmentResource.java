package com.shipments.service.interfaces.rest.resources;

import java.time.LocalDateTime;

public record UpdateShipmentResource(
    String destination,
    String description,
    LocalDateTime scheduledDate,
    String customerName,
    String customerPhone
) {}
