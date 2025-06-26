package com.shipments.service.domain.model.commands;

import java.time.LocalDateTime;

public record CreateShipmentCommand(
    String destination,
    String description,
    LocalDateTime scheduledDate,
    Long managerId,
    String customerName,
    String customerPhone
) {}
