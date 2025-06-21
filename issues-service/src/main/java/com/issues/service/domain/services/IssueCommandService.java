package com.issues.service.domain.services;

import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.domain.model.commands.CreateIssueCommand;
import com.issues.service.domain.model.commands.UpdateIssueCommand;

import java.util.Optional;

/**
 * Report Command Service Interface
 * This interface defines methods for handling commands related to reports.
 */
public interface IssueCommandService {

    /**
     * Handle Create Report Command
     * @param command the command to create a report
     * @return the created report
     */
    Issue handle(CreateIssueCommand command);

    /**
     * Handle Update Report Command
     * @param reportId the ID of the report to update
     * @param command the command to update the report
     * @return an Optional containing the updated report if successful, or empty if not found
     */
    Optional<Issue> handle(Long reportId, UpdateIssueCommand command);

    /**
     * Handle Delete Report Command
     * @param reportId the ID of the report to delete
     * @return true if the report was deleted successfully, false otherwise
     */
    boolean deleteIssue(Long reportId);
}
