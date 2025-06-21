package com.issues.service.domain.model.commands;

import com.issues.service.domain.model.valueobjects.IssueType;

import java.time.LocalDateTime;

public record CreateIssueCommand(String title, String content, IssueType type, LocalDateTime reportDate, Long carrierId, Long managerId, Long vehicleId, Long shipmentId) {}
