package com.shipments.service.domain.model.aggregates;

import com.shipments.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.shipments.service.domain.model.commands.CreateShipmentCommand;
import com.shipments.service.domain.model.valueobjects.ShipmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Shipment extends AuditableAbstractAggregateRoot<Shipment> {

    @NotBlank
    @Size(max = 100)
    private String destination;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private LocalDateTime scheduledDate;

    private LocalDateTime startedDate;

    private LocalDateTime completedDate;

    @NotNull
    private Long managerId;

    private Long carrierId;


    /**
     * Constructor with required fields.
     * @param destination destination of the shipment
     * @param description description of the shipment
     * @param scheduledDate date when the shipment is scheduled
     * @param managerId id of the manager who owns this shipment
     */
    public Shipment(String destination, String description, LocalDateTime scheduledDate, Long managerId){
        this.destination = destination;
        this.description = description;
        this.status = ShipmentStatus.PENDING;
        this.scheduledDate = scheduledDate;
        this.managerId = managerId;
    }

    public Shipment() {}

    public Shipment(CreateShipmentCommand command) {
        this.destination = command.destination();
        this.description = command.description();
        this.scheduledDate = command.scheduledDate();
        this.managerId = command.managerId();
        this.status = ShipmentStatus.PENDING;
    }

    public Shipment assignCarrier(Long carrierId) {
        this.carrierId = carrierId;
        return this;
    }

    public Shipment removeCarrier() {
        this.carrierId = null;
        return this;
    }

    public Shipment startShipment() {
        if (this.status != ShipmentStatus.PENDING) { throw new IllegalStateException("Cannot start shipment that is not in PENDING status"); }
        if (this.carrierId == null) { throw new IllegalStateException("Cannot start shipment without an assigned carrier"); }
        this.status = ShipmentStatus.IN_PROGRESS;
        this.startedDate = LocalDateTime.now();
        return this;
    }

    public Shipment completeShipment() {
        if (this.status != ShipmentStatus.IN_PROGRESS) { throw new IllegalStateException("Cannot complete shipment that is not in IN_PROGRESS status"); }
        this.status = ShipmentStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
        return this;
    }

    public Shipment cancelShipment() {
        if (this.status == ShipmentStatus.COMPLETED) { throw new IllegalStateException("Cannot cancel a shipment that is already completed"); }
        this.status = ShipmentStatus.CANCELLED;
        return this;
    }

    public Shipment changeStatus(ShipmentStatus status) {
        if (this.status == ShipmentStatus.COMPLETED && status != ShipmentStatus.COMPLETED) { throw new IllegalStateException("Cannot change status of a completed shipment"); }
        if (this.status == ShipmentStatus.PENDING && status == ShipmentStatus.PENDING) { throw new IllegalStateException("Cannot revert shipment status to PENDING"); }
        if (this.status == ShipmentStatus.IN_PROGRESS && this.startedDate == null) { this.startedDate = LocalDateTime.now(); }
        if (this.status == ShipmentStatus.COMPLETED && this.completedDate == null) { this.completedDate = LocalDateTime.now(); }
        this.status = status;
        return this;
    }
}

