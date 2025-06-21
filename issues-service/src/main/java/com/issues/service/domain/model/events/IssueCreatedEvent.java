package com.issues.service.domain.model.events;

import com.issues.service.domain.model.valueobjects.IssueType;

import java.time.LocalDateTime;

/**
 * Event published when an issue is created
 * @param issueId The ID of the newly created issue
 * @param title The title of the issue
 * @param content The content/description of the issue
 * @param type The type of the issue
 * @param reportDate The date when the issue was reported
 * @param carrierId The ID of the carrier who created the issue
 * @param managerId The ID of the manager associated with the issue
 * @param vehicleId The ID of the vehicle associated with the issue (if any)
 * @param shipmentId The ID of the shipment associated with the issue (if any)
 */
public record IssueCreatedEvent(
    Long issueId,
    String title,
    String content,
    IssueType type,
    LocalDateTime reportDate,
    Long carrierId,
    Long managerId,
    Long vehicleId,
    Long shipmentId
) {}
