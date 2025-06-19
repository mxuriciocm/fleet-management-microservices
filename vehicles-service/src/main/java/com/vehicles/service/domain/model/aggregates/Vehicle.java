package com.vehicles.service.domain.model.aggregates;


import com.vehicles.service.domain.model.commands.CreateVehicleCommand;
import com.vehicles.service.domain.model.valueobjects.VehicleStatus;
import com.vehicles.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    @NotBlank
    @Size(max = 20)
    @Column(unique = true)
    private String licensePlate;

    @NotBlank
    @Size(max = 50)
    private String brand;

    @NotBlank
    @Size(max = 50)
    private String model;

    @NotNull
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    private Long carrierId;

    private Long managerId;

    /**
     * Constructor with required fields.
     *
     * @param licensePlate license plate of the vehicle
     * @param brand brand of the vehicle
     * @param model model of the vehicle
     * @param managerId id of the manager who owns this vehicle
     */
    public Vehicle(String licensePlate, String brand, String model, Long managerId) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.status = VehicleStatus.ACTIVE;
        this.managerId = managerId;
    }

    public Vehicle() {}

    public Vehicle(CreateVehicleCommand command) {
        this.licensePlate = command.licensePlate();
        this.brand = command.brand();
        this.model = command.model();
        this.status = VehicleStatus.ACTIVE;
        this.managerId = command.managerId();
    }

    public Vehicle assignCarrier(Long carrierId) {
        this.carrierId = carrierId;
        return this;
    }

    public Vehicle removeCarrier() {
        this.carrierId = null;
        return this;
    }

    public Vehicle changeStatus(VehicleStatus status) {
        this.status = status;
        return this;
    }

}
