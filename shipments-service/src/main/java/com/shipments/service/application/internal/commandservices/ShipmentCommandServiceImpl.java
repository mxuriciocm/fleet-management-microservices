package com.shipments.service.application.internal.commandservices;

import com.shipments.service.application.events.EventsPublisher;
import com.shipments.service.domain.model.aggregates.Shipment;
import com.shipments.service.domain.model.commands.CreateShipmentCommand;
import com.shipments.service.domain.model.commands.UpdateShipmentCommand;
import com.shipments.service.domain.model.events.ShipmentCreatedEvent;
import com.shipments.service.domain.model.events.ShipmentUpdatedEvent;
import com.shipments.service.domain.model.valueobjects.ShipmentStatus;
import com.shipments.service.domain.services.ShipmentCommandService;
import com.shipments.service.infrastructure.persistence.jpa.repositories.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShipmentCommandServiceImpl implements ShipmentCommandService {
    private final ShipmentRepository shipmentRepository;
    private final EventsPublisher eventsPublisher;

    public ShipmentCommandServiceImpl(ShipmentRepository shipmentRepository, EventsPublisher eventsPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventsPublisher = eventsPublisher;
    }

    /**
     * Handles the creation of a new shipment.
     * @param command The {@link CreateShipmentCommand} Command
     * @return The created {@link Shipment} Aggregate
     */
    @Override
    public Shipment handle(CreateShipmentCommand command) {
        var shipment = new Shipment(command);
        var savedShipment = shipmentRepository.save(shipment);

        // Publicar evento de envío creado
        var event = new ShipmentCreatedEvent(
            savedShipment.getId(),
            savedShipment.getDestination(),
            savedShipment.getDescription(),
            savedShipment.getStatus(),
            savedShipment.getScheduledDate(),
            savedShipment.getManagerId(),
            savedShipment.getCustomerName(),
            savedShipment.getCustomerPhone()
        );
        eventsPublisher.publishShipmentCreatedEvent(event);

        return savedShipment;
    }

    /**
     * Handles the update of an existing shipment.
     * @param shipmentId The ID of the shipment to update
     * @param command The {@link UpdateShipmentCommand} Command
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> handle(Long shipmentId, UpdateShipmentCommand command) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    if (command.destination() != null) {shipment.setDestination(command.destination());}
                    if (command.description() != null) {shipment.setDescription(command.description());}
                    if (command.scheduledDate() != null) {shipment.setScheduledDate(command.scheduledDate());}
                    if (command.customerName() != null) {shipment.setCustomerName(command.customerName());}
                    if (command.customerPhone() != null) {shipment.setCustomerPhone(command.customerPhone());}

                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        command.destination(),
                        command.description(),
                        null, // No se actualiza el estado
                        command.scheduledDate(),
                        null, // No se actualiza el transportista
                        command.customerName(),
                        command.customerPhone()
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Assigns a carrier to a shipment.
     * @param shipmentId the shipment id
     * @param carrierId the carrier id
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> assignCarrier(Long shipmentId, Long carrierId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.assignCarrier(carrierId);
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con asignación de transportista
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null, // No se actualiza el destino
                        null, // No se actualiza la descripción
                        updatedShipment.getStatus(), // Incluir estado actualizado (ASSIGNED)
                        null, // No se actualiza la fecha programada
                        carrierId,
                        null, // No se actualizan datos del cliente
                        null  // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Removes a carrier from a shipment.
     * @param shipmentId the shipment id
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> removeCarrier(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.removeCarrier();
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con eliminación de transportista
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null,  // No se actualiza el destino
                        null,  // No se actualiza la descripción
                        updatedShipment.getStatus(), // Incluir estado actualizado (vuelve a PENDING)
                        null,  // No se actualiza la fecha programada
                        null,  // El transportista ha sido eliminado (null)
                        null,  // No se actualizan datos del cliente
                        null   // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Updates the status of a shipment.
     * @param shipmentId the shipment id
     * @param status the new status
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> updateStatus(Long shipmentId, ShipmentStatus status) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.changeStatus(status);
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con cambio de estado
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null,  // No se actualiza el destino
                        null,  // No se actualiza la descripción
                        status,
                        null,  // No se actualiza la fecha programada
                        null,  // No se actualiza el transportista
                        null,  // No se actualizan datos del cliente
                        null   // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Starts a shipment.
     * @param shipmentId the shipment id
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> startShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.startShipment();
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con cambio de estado a EN_PROGRESO
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null, // No se actualiza el destino
                        null, // No se actualiza la descripción
                        ShipmentStatus.IN_PROGRESS,
                        null, // No se actualiza la fecha programada
                        null, // No se actualiza el transportista
                        null, // No se actualizan datos del cliente
                        null  // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Completes a shipment.
     * @param shipmentId the shipment id
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> completeShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.completeShipment();
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con cambio de estado a COMPLETADO
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null, // No se actualiza el destino
                        null, // No se actualiza la descripción
                        ShipmentStatus.COMPLETED,
                        null, // No se actualiza la fecha programada
                        null, // No se actualiza el transportista
                        null, // No se actualizan datos del cliente
                        null  // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    /**
     * Cancels a shipment.
     * @param shipmentId the shipment id
     * @return An {@link Optional} containing the updated {@link Shipment} Aggregate, or empty if not found
     */
    @Override
    public Optional<Shipment> cancelShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    shipment.cancelShipment();
                    var updatedShipment = shipmentRepository.save(shipment);

                    // Publicar evento de envío actualizado con cambio de estado a CANCELADO
                    var event = new ShipmentUpdatedEvent(
                        updatedShipment.getId(),
                        null, // No se actualiza el destino
                        null, // No se actualiza la descripción
                        ShipmentStatus.CANCELLED,
                        null, // No se actualiza la fecha programada
                        null, // No se actualiza el transportista
                        null, // No se actualizan datos del cliente
                        null  // No se actualizan datos del cliente
                    );
                    eventsPublisher.publishShipmentUpdatedEvent(event);

                    return updatedShipment;
                });
    }

    @Override
    public boolean deleteShipment(Long shipmentId) {
        if (shipmentRepository.existsById(shipmentId)) {
            shipmentRepository.deleteById(shipmentId);
            return true;
        }
        return false;
    }
}
