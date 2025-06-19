package com.vehicles.service.domain.model.events;

import com.vehicles.service.domain.model.valueobjects.VehicleStatus;

/**
 * Event published when a vehicle is updated
 * @param vehicleId The ID of the updated vehicle
 * @param licensePlate The license plate of the vehicle (null if not changed)
 * @param brand The brand of the vehicle (null if not changed)
 * @param model The model of the vehicle (null if not changed)
 * @param status The status of the vehicle (null if not changed)
 * @param carrierId The ID of the carrier assigned to the vehicle (null if not assigned/changed)
 */
public record VehicleUpdatedEvent(
    Long vehicleId,
    String licensePlate,
    String brand,
    String model,
    VehicleStatus status,
    Long carrierId
) {}
