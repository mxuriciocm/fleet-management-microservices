package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.entities.Role;
import com.iam.service.interfaces.rest.resources.RoleResource;

/**
 * Assembler to convert a Role entity to a RoleResource.
 * <p>
 *     This class is used to convert a Role entity to a RoleResource.
 * </p>
 */
public class RoleResourceFromEntityAssembler {
    /**
     * Converts a Role entity to a RoleResource.
     *
     * @param entity The Role entity to convert.
     * @return The RoleResource.
     */
    public static RoleResource toResourceFromEntity(Role entity) {
        return new RoleResource(
                entity.getId(),
                entity.getStringName());
    }
}
