package com.issues.service.interfaces.rest.resources;

import com.issues.service.domain.model.valueobjects.IssueType;

import java.time.LocalDateTime;

public record IssueResource(Long id, String title, String content, IssueType type, LocalDateTime reportDate, Long carrierId, Long managerId, Long vehicleId, Long shipmentId) {}
