package com.vehicles.service.domain.services;

import com.vehicles.service.domain.model.aggregates.Vehicle;
import com.vehicles.service.domain.model.commands.CreateVehicleCommand;
import com.vehicles.service.domain.model.commands.UpdateVehicleCommand;
import com.vehicles.service.domain.model.valueobjects.VehicleStatus;

import java.util.Optional;

/**
 * Vehicle Command Service
 */
public interface VehicleCommandService {

    /**
     * Handle Create Vehicle Command
     *
     * @param command The {@link CreateVehicleCommand} Command
     * @return The created vehicle
     */
    Vehicle handle(CreateVehicleCommand command);

    /**
     * Handle Update Vehicle Command
     *
     * @param vehicleId the vehicle id
     * @param command The {@link UpdateVehicleCommand} Command
     * @return An {@link Optional< Vehicle >} instance if the vehicle was updated successfully, otherwise empty
     */
    Optional<Vehicle> handle(Long vehicleId, UpdateVehicleCommand command);

    /**
     * Handle assigning a carrier to a vehicle
     * @param vehicleId the vehicle id
     * @param carrierId the carrier id
     * @return An {@link Optional< Vehicle >} instance if the vehicle was updated successfully, otherwise empty
     */
    Optional<Vehicle> handle(Long vehicleId, Long carrierId);

    /**
     * Handle updating the status of a vehicle
     *
     * @param vehicleId the vehicle id
     * @param status the new status of the vehicle
     * @return An {@link Optional< Vehicle >} instance if the vehicle was updated successfully, otherwise empty
     */
    Optional<Vehicle> handle(Long vehicleId, VehicleStatus status);

    /**
     * Handle deleting a vehicle
     *
     * @param vehicleId the vehicle id
     * @return An {@link Optional< Vehicle >} instance if the vehicle was deleted successfully, otherwise empty
     */
    Optional<Vehicle> handle(Long vehicleId);

    /**
     * Check if the vehicle limit has been reached for a manager
     *
     * @param managerId the manager id
     * @param isPro whether the manager is a pro user
     * @return true if the vehicle limit has been reached, false otherwise
     */
    boolean hasReachedVehicleLimit(Long managerId, boolean isPro);
}
