package com.profiles.service.interfaces.rest.transform;

import com.profiles.service.domain.model.aggregates.Profile;
import com.profiles.service.interfaces.rest.resources.ProfileResource;

public class ProfileResourceFromEntityAssembler {
    public static ProfileResource toResourceFromEntity(Profile entity) {
        return new ProfileResource(entity.getUserId(), entity.getFullName(), entity.getPhoneNumber());
    }
}
