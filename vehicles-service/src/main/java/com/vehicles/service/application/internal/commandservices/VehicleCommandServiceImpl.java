package com.vehicles.service.application.internal.commandservices;

import com.vehicles.service.application.events.EventsPublisher;
import com.vehicles.service.domain.model.aggregates.Vehicle;
import com.vehicles.service.domain.model.commands.CreateVehicleCommand;
import com.vehicles.service.domain.model.commands.UpdateVehicleCommand;
import com.vehicles.service.domain.model.events.VehicleCreatedEvent;
import com.vehicles.service.domain.model.events.VehicleUpdatedEvent;
import com.vehicles.service.domain.model.valueobjects.VehicleStatus;
import com.vehicles.service.domain.services.VehicleCommandService;
import com.vehicles.service.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private static final int NON_PRO_VEHICLE_LIMIT = 10;
    private final VehicleRepository vehicleRepository;
    private final EventsPublisher eventsPublisher;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository, EventsPublisher eventsPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.eventsPublisher = eventsPublisher;
    }

    /**
     * Create a new vehicle.
     * @param command the command containing the vehicle details
     * @return the created vehicle
     */
    @Override
    public Vehicle handle(CreateVehicleCommand command) {
        var vehicle = new Vehicle(command);
        var savedVehicle = vehicleRepository.save(vehicle);

        // Publicar evento de vehículo creado
        var event = new VehicleCreatedEvent(
            savedVehicle.getId(),
            savedVehicle.getLicensePlate(),
            savedVehicle.getBrand(),
            savedVehicle.getModel(),
            savedVehicle.getStatus(),
            savedVehicle.getManagerId()
        );
        eventsPublisher.publishVehicleCreatedEvent(event);

        return savedVehicle;
    }

    /**
     * Update a vehicle's details.
     * @param vehicleId the vehicle id
     * @param command the command containing the updated details
     * @return an Optional containing the updated vehicle if successful, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(Long vehicleId, UpdateVehicleCommand command) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    if (command.brand() != null) { vehicle.setBrand(command.brand()); }
                    if (command.model() != null) { vehicle.setModel(command.model()); }
                    var updatedVehicle = vehicleRepository.save(vehicle);

                    // Publicar evento de vehículo actualizado
                    var event = new VehicleUpdatedEvent(
                        updatedVehicle.getId(),
                        null, // No se actualiza la placa
                        command.brand(),
                        command.model(),
                        null, // No se actualiza el estado
                        null  // No se actualiza el carrier
                    );
                    eventsPublisher.publishVehicleUpdatedEvent(event);

                    return updatedVehicle;
                });
    }

    /**
     * Assign a carrier to a vehicle.
     * @param vehicleId the vehicle id
     * @param carrierId the carrier id
     * @return an Optional containing the updated vehicle if successful, or empty if not found or if carrier already has a vehicle
     */
    @Override
    public Optional<Vehicle> handle(Long vehicleId, Long carrierId) {
        if (vehicleRepository.findByCarrierId(carrierId).isPresent()) { return Optional.empty(); }
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.assignCarrier(carrierId);
                    var updatedVehicle = vehicleRepository.save(vehicle);
                    var event = new VehicleUpdatedEvent(
                        updatedVehicle.getId(),
                        null, // No se actualiza la placa
                        null, // No se actualiza la marca
                        null, // No se actualiza el modelo
                        null, // No se actualiza el estado
                        carrierId
                    );
                    eventsPublisher.publishVehicleUpdatedEvent(event);

                    return updatedVehicle;
                });
    }

    /**
     * Change the status of a vehicle.
     * @param vehicleId the vehicle id
     * @param status the new status of the vehicle
     * @return an Optional containing the updated vehicle if successful, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(Long vehicleId, VehicleStatus status) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.changeStatus(status);
                    var updatedVehicle = vehicleRepository.save(vehicle);

                    // Publicar evento de vehículo actualizado con cambio de estado
                    var event = new VehicleUpdatedEvent(
                        updatedVehicle.getId(),
                        null, // No se actualiza la placa
                        null, // No se actualiza la marca
                        null, // No se actualiza el modelo
                        status,
                        null  // No se actualiza el carrier
                    );
                    eventsPublisher.publishVehicleUpdatedEvent(event);

                    return updatedVehicle;
                });
    }

    /**
     * Remove the carrier from a vehicle.
     * @param vehicleId the vehicle id
     * @return an Optional containing the updated vehicle if successful, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.removeCarrier();
                    var updatedVehicle = vehicleRepository.save(vehicle);

                    // Publicar evento de vehículo actualizado con eliminación de carrier
                    var event = new VehicleUpdatedEvent(
                        updatedVehicle.getId(),
                        null,  // No se actualiza la placa
                        null,  // No se actualiza la marca
                        null,  // No se actualiza el modelo
                        null,  // No se actualiza el estado
                        null   // El carrier ha sido eliminado (null)
                    );
                    eventsPublisher.publishVehicleUpdatedEvent(event);

                    return updatedVehicle;
                });
    }

    /**
     * Check if the manager has reached the vehicle limit.
     * @param managerId the manager id
     * @param isPro true if the user is a PRO user, false otherwise
     * @return true if the manager has reached the vehicle limit, false otherwise
     */
    @Override
    public boolean hasReachedVehicleLimit(Long managerId, boolean isPro) {
        if (isPro) {
            return false;
        } else {
            int vehicleCount = vehicleRepository.countByManagerId(managerId);
            return vehicleCount >= NON_PRO_VEHICLE_LIMIT;
        }
    }
}
