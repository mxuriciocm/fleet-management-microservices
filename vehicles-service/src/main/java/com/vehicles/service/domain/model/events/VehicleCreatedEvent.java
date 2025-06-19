package com.vehicles.service.domain.model.events;

import com.vehicles.service.domain.model.valueobjects.VehicleStatus;

/**
 * Event published when a vehicle is created
 * @param vehicleId The ID of the newly created vehicle
 * @param licensePlate The license plate of the vehicle
 * @param brand The brand of the vehicle
 * @param model The model of the vehicle
 * @param status The status of the vehicle
 * @param managerId The ID of the manager who created the vehicle
 */
public record VehicleCreatedEvent(
    Long vehicleId,
    String licensePlate,
    String brand,
    String model,
    VehicleStatus status,
    Long managerId
) {}
