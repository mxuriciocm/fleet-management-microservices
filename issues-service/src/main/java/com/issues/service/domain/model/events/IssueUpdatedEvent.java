package com.issues.service.domain.model.events;

import com.issues.service.domain.model.valueobjects.IssueType;

/**
 * Event published when an issue is updated
 * @param issueId The ID of the updated issue
 * @param title The updated title of the issue (null if not changed)
 * @param content The updated content/description of the issue (null if not changed)
 * @param type The updated type of the issue (null if not changed)
 */
public record IssueUpdatedEvent(
    Long issueId,
    String title,
    String content,
    IssueType type
) {}
