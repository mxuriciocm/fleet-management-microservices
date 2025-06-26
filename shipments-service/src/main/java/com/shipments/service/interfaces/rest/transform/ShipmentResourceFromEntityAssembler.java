package com.shipments.service.interfaces.rest.transform;

import com.shipments.service.domain.model.aggregates.Shipment;
import com.shipments.service.interfaces.rest.resources.ShipmentResource;

import java.util.List;
import java.util.stream.Collectors;

public class ShipmentResourceFromEntityAssembler {
    public static ShipmentResource toResourceFromEntity(Shipment entity) {
        return new ShipmentResource(
                entity.getId(),
                entity.getDestination(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getScheduledDate(),
                entity.getStartedDate(),
                entity.getCompletedDate(),
                entity.getManagerId(),
                entity.getCarrierId(),
                entity.getCustomerName(),
                entity.getCustomerPhone()
        );
    }

    public static List<ShipmentResource> toResourceFromEntities(List<Shipment> entities) {
        return entities.stream()
                .map(ShipmentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
    }
}
