package com.vehicles.service.interfaces.rest.resources;

import com.vehicles.service.domain.model.valueobjects.VehicleStatus;

public record VehicleResource(Long id, String licensePlate, String brand, String model, VehicleStatus status, Long carrierId, Long managerId) {}
