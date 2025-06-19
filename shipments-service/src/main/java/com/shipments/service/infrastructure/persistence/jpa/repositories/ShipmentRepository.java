package com.shipments.service.infrastructure.persistence.jpa.repositories;

import com.shipments.service.domain.model.aggregates.Shipment;
import com.shipments.service.domain.model.valueobjects.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Finds all shipments with the given status.
     * @param status the status of the shipments to find
     * @return a list of shipments with the specified status
     */
    List<Shipment> findByStatus(ShipmentStatus status);

    /**
     * Finds all shipments assigned to a specific manager.
     * @param managerId the ID of the manager
     * @return a list of shipments assigned to the specified manager
     */
    List<Shipment> findByManagerId(Long managerId);

    /**
     * Finds all shipments assigned to a specific carrier.
     * @param carrierId the ID of the carrier
     * @return a list of shipments assigned to the specified carrier
     */
    List<Shipment> findByCarrierId(Long carrierId);

    /**
     * Finds all shipments assigned to a specific manager with a specific status.
     * @param managerId the ID of the manager
     * @param status the status of the shipments to find
     * @return a list of shipments assigned to the specified manager with the specified status
     */
    List<Shipment> findByManagerIdAndStatus(Long managerId, ShipmentStatus status);

    /**
     * Finds all shipments assigned to a specific carrier with a specific status.
     * @param carrierId the ID of the carrier
     * @param status the status of the shipments to find
     * @return a list of shipments assigned to the specified carrier with the specified status
     */
    List<Shipment> findByCarrierIdAndStatus(Long carrierId, ShipmentStatus status);
}
