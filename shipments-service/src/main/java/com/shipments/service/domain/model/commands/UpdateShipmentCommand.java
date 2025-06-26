package com.shipments.service.domain.model.commands;

import java.time.LocalDateTime;

public record UpdateShipmentCommand(
    String destination,
    String description,
    LocalDateTime scheduledDate,
    String customerName,
    String customerPhone
) {}
