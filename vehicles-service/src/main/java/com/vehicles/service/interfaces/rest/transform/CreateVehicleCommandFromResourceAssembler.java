package com.vehicles.service.interfaces.rest.transform;

import com.vehicles.service.domain.model.commands.CreateVehicleCommand;
import com.vehicles.service.interfaces.rest.resources.CreateVehicleResource;

public class CreateVehicleCommandFromResourceAssembler {
    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource, Long managerId) {
        return new CreateVehicleCommand(resource.licensePlate(), resource.brand(), resource.model(), managerId);
    }
}
