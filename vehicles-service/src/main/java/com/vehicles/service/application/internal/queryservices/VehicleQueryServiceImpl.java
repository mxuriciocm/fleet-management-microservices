package com.vehicles.service.application.internal.queryservices;

import com.vehicles.service.domain.model.aggregates.Vehicle;
import com.vehicles.service.domain.model.queries.GetVehicleByCarrierIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByLicensePlateQuery;
import com.vehicles.service.domain.model.queries.GetVehiclesByManagerIdQuery;
import com.vehicles.service.domain.services.VehicleQueryService;
import com.vehicles.service.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;

    public VehicleQueryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Retrieve a vehicle by its ID.
     * @param query the query containing the vehicle ID
     * @return an Optional containing the vehicle if found, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId());
    }

    /**
     * Retrieve all vehicles managed by a specific manager.
     * @param query the query containing the manager ID
     * @return a list of vehicles managed by the specified manager
     */
    @Override
    public List<Vehicle> handle(GetVehiclesByManagerIdQuery query) {
        return vehicleRepository.findByManagerId(query.managerId());
    }

    /**
     * Retrieve a vehicle by its carrier ID.
     * @param query the query containing the carrier ID
     * @return an Optional containing the vehicle if found, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(GetVehicleByCarrierIdQuery query) {
        return vehicleRepository.findByCarrierId(query.carrierId());
    }

    /**
     * Retrieve a vehicle by its license plate.
     * @param query the query containing the license plate
     * @return an Optional containing the vehicle if found, or empty if not found
     */
    @Override
    public Optional<Vehicle> handle(GetVehicleByLicensePlateQuery query) {
        return vehicleRepository.findByLicensePlate(query.licensePlate());
    }

    /**
     * Count the number of vehicles managed by a specific manager.
     * @param managerId the ID of the manager
     * @return the count of vehicles managed by the specified manager
     */
    @Override
    public int countVehiclesByManagerId(Long managerId) {
        return vehicleRepository.countByManagerId(managerId);
    }
}
