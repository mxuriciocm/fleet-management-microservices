package com.issues.service.interfaces.rest.transform;

import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.interfaces.rest.resources.IssueResource;

import java.util.List;

public class IssueResourceFromEntityAssembler {
    public static IssueResource toResourceFromEntity(Issue entity){
        return new IssueResource(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getType(),
                entity.getReportDate(),
                entity.getCarrierId(),
                entity.getManagerId(),
                entity.getVehicleId(),
                entity.getShipmentId()
        );
    }

    public static List<IssueResource> toResourceFromEntities(List<Issue> entities){
        return entities.stream()
                .map(IssueResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
