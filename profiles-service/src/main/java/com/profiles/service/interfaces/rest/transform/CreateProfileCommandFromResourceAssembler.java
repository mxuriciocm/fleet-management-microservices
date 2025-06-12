package com.profiles.service.interfaces.rest.transform;

import com.profiles.service.domain.model.commands.CreateProfileCommand;
import com.profiles.service.interfaces.rest.resources.CreateProfileResource;

public class CreateProfileCommandFromResourceAssembler {
    public static CreateProfileCommand toCommandFromResource(CreateProfileResource resource) {
        return new CreateProfileCommand(resource.userId(), resource.firstName(), resource.lastName(), resource.phoneNumber());
    }
}
