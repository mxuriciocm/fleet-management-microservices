package com.shipments.service.interfaces.rest.transform;

import com.shipments.service.domain.model.commands.UpdateShipmentCommand;
import com.shipments.service.interfaces.rest.resources.UpdateShipmentResource;

public class UpdateShipmentCommandFromResourceAssembler {
    public static UpdateShipmentCommand toCommandFromResource(UpdateShipmentResource resource) {
        return new UpdateShipmentCommand(
            resource.destination(),
            resource.description(),
            resource.scheduledDate(),
            resource.customerName(),
            resource.customerPhone()
        );
    }
}
