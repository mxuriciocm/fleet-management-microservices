package com.issues.service.interfaces.rest.transform;

import com.issues.service.domain.model.commands.CreateIssueCommand;
import com.issues.service.interfaces.rest.resources.CreateIssueResource;

import java.time.LocalDateTime;

public class CreateIssueCommandFromResourceAssembler {
    public static CreateIssueCommand toCommandFromResource(CreateIssueResource resource, Long carrierId, Long managerId, Long vehicleId) {
        return new CreateIssueCommand(
                resource.title(),
                resource.content(),
                resource.type(),
                LocalDateTime.now(),
                carrierId,
                managerId,
                vehicleId,
                resource.shipmentId()
        );
    }
}
