package com.issues.service.domain.model.commands;

import com.issues.service.domain.model.valueobjects.IssueType;

public record UpdateIssueCommand(String title, String content, IssueType type) {}
