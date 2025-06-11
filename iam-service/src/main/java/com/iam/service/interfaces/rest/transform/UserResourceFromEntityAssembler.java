package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.aggregates.User;
import com.iam.service.interfaces.rest.resources.UserResource;

/**
 * Assembler to convert a User entity to a UserResource.
 * <p>
 *     This class is used to convert a User entity to a UserResource.
 * </p>
 */
public class UserResourceFromEntityAssembler {
    /**
     * Converts a User entity to a UserResource.
     *
     * @param entity The User entity to convert.
     * @return The UserResource.
     */
    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(
                entity.getId(),
                entity.getEmail(),
                RoleStringListFromEntityListAssembler.toResourceListFromEntitySet(entity.getRoles()));
    }
}
