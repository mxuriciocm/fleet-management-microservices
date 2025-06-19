package com.shipments.service.interfaces.rest.resources;

import java.time.LocalDateTime;

public record CreateShipmentResource(String destination, String description, LocalDateTime scheduledDate) {
    public CreateShipmentResource {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
        if (scheduledDate == null) {
            throw new IllegalArgumentException("Scheduled date cannot be null");
        }
    }
}
