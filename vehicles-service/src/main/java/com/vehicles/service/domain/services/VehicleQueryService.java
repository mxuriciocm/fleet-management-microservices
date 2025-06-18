package com.vehicles.service.domain.services;

import com.vehicles.service.domain.model.aggregates.Vehicle;
import com.vehicles.service.domain.model.queries.GetVehicleByCarrierIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByLicensePlateQuery;
import com.vehicles.service.domain.model.queries.GetVehiclesByManagerIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Vehicle Query Service
 */
public interface VehicleQueryService {

    /**
     * Handle get vehicle by ID query
     *
     * @param query the query containing the vehicle ID
     * @return the vehicle if found
     */
    Optional<Vehicle> handle(GetVehicleByIdQuery query);

    /**
     * Handle get vehicles by manager ID query
     *
     * @param query the query containing the manager ID
     * @return list of vehicles
     */
    List<Vehicle> handle(GetVehiclesByManagerIdQuery query);

    /**
     * Handle get vehicle by carrier ID query
     *
     * @param query the query containing the carrier ID
     * @return the vehicle if found
     */
    Optional<Vehicle> handle(GetVehicleByCarrierIdQuery query);

    /**
     * Handle get vehicle by license plate query
     *
     * @param query the query containing the license plate
     * @return the vehicle if found
     */
    Optional<Vehicle> handle(GetVehicleByLicensePlateQuery query);

    /**
     * Count vehicles by manager id
     *
     * @param managerId the manager id
     * @return the number of vehicles
     */
    int countVehiclesByManagerId(Long managerId);
}
