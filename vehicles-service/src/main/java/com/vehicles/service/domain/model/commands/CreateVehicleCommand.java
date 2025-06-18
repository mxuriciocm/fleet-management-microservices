package com.vehicles.service.domain.model.commands;

/**
 * Create Vehicle Command
 * @param licensePlate The license plate of the vehicle
 * @param brand The brand/manufacturer of the vehicle
 * @param model The model of the vehicle
 * @param managerId The ID of the manager who owns the vehicle
 */
public record CreateVehicleCommand(String licensePlate, String brand, String model, Long managerId) {}

