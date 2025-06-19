package com.vehicles.service.interfaces.rest.transform;

import com.vehicles.service.domain.model.commands.UpdateVehicleCommand;
import com.vehicles.service.interfaces.rest.resources.UpdateVehicleResource;

public class UpdateVehicleCommandFromResourceAssembler {
    public static UpdateVehicleCommand toCommandFromResource(UpdateVehicleResource resource) {
        return new UpdateVehicleCommand(resource.brand(), resource.model());
    }
}
