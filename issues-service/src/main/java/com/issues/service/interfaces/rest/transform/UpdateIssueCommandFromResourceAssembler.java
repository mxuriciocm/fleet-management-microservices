package com.issues.service.interfaces.rest.transform;

import com.issues.service.domain.model.commands.UpdateIssueCommand;
import com.issues.service.interfaces.rest.resources.UpdateIssueResource;

public class UpdateIssueCommandFromResourceAssembler {
    public static UpdateIssueCommand toCommandFromResource(UpdateIssueResource resource) {
        return new UpdateIssueCommand(resource.title(), resource.content(), resource.type());
    }
}
