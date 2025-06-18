package com.vehicles.service.domain.model.commands;

/**
 * Update Vehicle Command
 * @param brand The brand/manufacturer of the vehicle
 * @param model The model of the vehicle
 */
public record UpdateVehicleCommand(String brand, String model) {}
