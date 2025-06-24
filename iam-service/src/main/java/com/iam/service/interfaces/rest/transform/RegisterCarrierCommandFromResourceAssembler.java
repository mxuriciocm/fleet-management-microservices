package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.commands.RegisterCarrierCommand;
import com.iam.service.interfaces.rest.resources.RegisterCarrierResource;

/**
 * Assembler to convert a RegisterCarrierResource to a RegisterCarrierCommand.
 */
public class RegisterCarrierCommandFromResourceAssembler {

    /**
     * Converts a RegisterCarrierResource to a RegisterCarrierCommand.
     *
     * @param resource The resource to convert
     * @param managerId The ID of the manager registering the carrier
     * @return The RegisterCarrierCommand
     */
    public static RegisterCarrierCommand toCommandFromResource(RegisterCarrierResource resource, Long managerId) {
        var roles = RoleListFromStringAssembler.toRoleListFromStringList(resource.roles());
        return new RegisterCarrierCommand(resource.email(), resource.password(), roles, managerId);
    }
}
