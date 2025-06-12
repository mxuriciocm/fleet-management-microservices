package com.profiles.service.interfaces.rest.transform;

import com.profiles.service.domain.model.commands.UpdateProfileCommand;
import com.profiles.service.interfaces.rest.resources.UpdateProfileResource;

public class UpdateProfileCommandFromResourceAssembler {

    public static UpdateProfileCommand toCommandFromResource(UpdateProfileResource resource, Long userId) {
        return new UpdateProfileCommand(userId, resource.firstName(), resource.lastName(), resource.phoneNumber());
    }
}
