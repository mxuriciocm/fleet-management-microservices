package com.issues.service.domain.services;

import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface IssueQueryService {

    /**
     * Handle Get Report by ID Query
     * @param query the query containing the report ID
     * @return an Optional containing the Report if found, or empty if not found
     */
    Optional<Issue> handle(GetIssueByIdQuery query);

    /**
     * Handle Get Reports carrier ID Query
     * @param query the query containing the criteria
     * @return a List of Reports associated with the specified carrier ID
     */
    List<Issue> handle(GetIssuesByCarrierIdQuery query);

    /**
     * Handle Get Reports by Manager ID Query
     * @param query the query containing the manager ID
     * @return a List of Reports associated with the specified manager ID
     */
    List<Issue> handle(GetIssuesByManagerIdQuery query);

    /**
     * Handle Get Reports by Type Query
     * @param query the query containing the report type
     * @return a List of Reports of the specified type
     */
    List<Issue> handle(GetIssuesByTypeQuery query);

    /**
     * Handle Get Reports by Vehicle ID Query
     * @param query the query containing the vehicle ID
     * @return a List of Reports associated with the specified vehicle ID
     */
    List<Issue> handle(GetIssuesByVehicleIdQuery query);

    /**
     * Handle Get Reports by Date Range Query
     * @param query the query containing the start and end dates
     * @return a List of Reports within the specified date range
     */
    List<Issue> handle(GetIssuesByShipmentIdQuery query);
}
