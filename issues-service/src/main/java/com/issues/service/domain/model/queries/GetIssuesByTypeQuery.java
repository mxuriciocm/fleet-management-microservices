package com.issues.service.domain.model.queries;

import com.issues.service.domain.model.valueobjects.IssueType;

public record GetIssuesByTypeQuery(IssueType type) {}
