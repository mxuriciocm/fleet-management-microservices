package com.vehicles.service.infrastructure.persistence.jpa.repositories;

import com.vehicles.service.domain.model.aggregates.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds a vehicle by its unique license plate.
     * @param licensePlate the license plate of the vehicle
     * @return the vehicle with the specified license plate, if it exists
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Finds all vehicles managed by a specific manager.
     * @param managerId the ID of the manager
     * @return a list of vehicles managed by the specified manager
     */
    List<Vehicle> findByManagerId(Long managerId);

    /**
     * Finds a vehicle assigned to a specific carrier.
     * @param carrierId the ID of the carrier
     * @return the vehicle assigned to the specified carrier, if it exists
     */
    Optional<Vehicle> findByCarrierId(Long carrierId);

    /**
     * Counts the number of vehicles managed by a specific manager.
     * @param managerId the ID of the manager
     * @return the count of vehicles managed by the specified manager
     */
    int countByManagerId(Long managerId);
}
