package com.issues.service.infrastructure.persistence.jpa.repositories;

import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.domain.model.valueobjects.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Find reports by carrier ID.
     * @param carrierId the ID of the carrier
     * @return a List of Reports associated with the specified carrier ID
     */
    List<Issue> findByCarrierId(Long carrierId);

    /**
     * Find reports by manager ID.
     * @param managerId the ID of the manager
     * @return a List of Reports associated with the specified manager ID
     */
    List<Issue> findByManagerId(Long managerId);

    /**
     * Find reports by type.
     * @param type the type of report
     * @return a List of Reports of the specified type
     */
    List<Issue> findByType(IssueType type);

    /**
     * Find reports by carrier ID and type.
     * @param carrierId the ID of the carrier
     * @param type the type of report
     * @return a List of Reports associated with the specified carrier ID and type
     */
    List<Issue> findByCarrierIdAndType(Long carrierId, IssueType type);

    /**
     * Find reports by manager ID and type.
     * @param managerId the ID of the manager
     * @param type the type of report
     * @return a List of Reports associated with the specified manager ID and type
     */
    List<Issue> findByManagerIdAndType(Long managerId, IssueType type);

    /**
     * Find reports by vehicle ID.
     * @param vehicleId the ID of the vehicle
     * @return a List of Reports associated with the specified vehicle ID
     */
    List<Issue> findByVehicleId(Long vehicleId);

    /**
     * Find reports by shipment ID.
     * @param shipmentId the ID of the shipment
     * @return a List of Reports associated with the specified shipment ID
     */
    List<Issue> findByShipmentId(Long shipmentId);
}
