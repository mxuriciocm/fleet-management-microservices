package com.shipments.service.domain.services;

import com.shipments.service.domain.model.aggregates.Shipment;
import com.shipments.service.domain.model.commands.CreateShipmentCommand;
import com.shipments.service.domain.model.commands.UpdateShipmentCommand;
import com.shipments.service.domain.model.valueobjects.ShipmentStatus;

import java.util.Optional;

public interface ShipmentCommandService {
    /**
     * Handle Create Shipment Command
     * @param command The {@link CreateShipmentCommand} Command
     * @return The created shipment
     */
    Shipment handle(CreateShipmentCommand command);

    /**
     * Handle Update Shipment Command
     * @param shipmentId the shipment id
     * @param command The {@link UpdateShipmentCommand} Command
     * @return An {@link Optional <Shipment>} instance if the shipment was updated successfully, otherwise empty
     */
    Optional<Shipment> handle(Long shipmentId, UpdateShipmentCommand command);

    /**
     * Assign a carrier to a shipment
     * @param shipmentId the shipment id
     * @param carrierId the carrier id
     * @return An {@link Optional<Shipment>} instance if the carrier was assigned successfully, otherwise empty
     */
    Optional<Shipment> assignCarrier(Long shipmentId, Long carrierId);

    /**
     * Update the status of a shipment
     * @param shipmentId the shipment id
     * @param status the new status of the shipment
     * @return An {@link Optional<Shipment>} instance if the status was updated successfully, otherwise empty
     */
    Optional<Shipment> updateStatus(Long shipmentId, ShipmentStatus status);

    /**
     * Remove the carrier from a shipment
     * @param shipmentId the shipment id
     * @return An {@link Optional<Shipment>} instance if the carrier was removed successfully, otherwise empty
     */
    Optional<Shipment> removeCarrier(Long shipmentId);

    /**
     * Start, complete or cancel a shipment
     * @param shipmentId the shipment id
     * @return An {@link Optional<Shipment>} instance if the shipment was started, completed or canceled successfully, otherwise empty
     */
    Optional<Shipment> startShipment(Long shipmentId);

    Optional<Shipment> completeShipment(Long shipmentId);

    Optional<Shipment> cancelShipment(Long shipmentId);

    boolean deleteShipment(Long shipmentId);
}
