package com.shipments.service.interfaces.rest.transform;

import com.shipments.service.domain.model.commands.CreateShipmentCommand;
import com.shipments.service.interfaces.rest.resources.CreateShipmentResource;

public class CreateShipmentCommandFromResourceAssembler {
    public static CreateShipmentCommand toCommandFromResource(CreateShipmentResource resource, Long managerId) {
        return new CreateShipmentCommand(
            resource.destination(),
            resource.description(),
            resource.scheduledDate(),
            managerId,
            resource.customerName(),
            resource.customerPhone()
        );
    }
}
