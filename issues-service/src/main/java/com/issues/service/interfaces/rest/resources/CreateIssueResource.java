package com.issues.service.interfaces.rest.resources;

import com.issues.service.domain.model.valueobjects.IssueType;

public record CreateIssueResource(String title, String content, IssueType type, Long shipmentId) {
    public CreateIssueResource {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Report type cannot be null");
        }
    }
}
