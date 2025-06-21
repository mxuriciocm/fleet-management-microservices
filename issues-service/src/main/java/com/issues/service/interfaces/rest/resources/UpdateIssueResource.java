package com.issues.service.interfaces.rest.resources;

import com.issues.service.domain.model.valueobjects.IssueType;

public record UpdateIssueResource(String title, String content, IssueType type) {}
