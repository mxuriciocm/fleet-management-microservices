package com.vehicles.service.interfaces.rest.resources;

public record CreateVehicleResource(String licensePlate, String brand, String model) {
    public CreateVehicleResource {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or blank");
        }
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand cannot be null or blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be null or blank");
        }
    }
}
