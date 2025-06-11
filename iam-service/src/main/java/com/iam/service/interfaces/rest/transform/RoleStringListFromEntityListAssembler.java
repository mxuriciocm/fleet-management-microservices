package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.entities.Role;

import java.util.List;
import java.util.Set;

/**
 * Assembler to convert a list of Role entities to a list of role names.
 * <p>
 *     This class is used to convert a list of Role entities to a list of role names.
 * </p>
 */
public class RoleStringListFromEntityListAssembler {
    /**
     * Converts a list of Role entities to a list of role names.
     *
     * @param entity The list of Role entities to convert.
     * @return The list of role names.
     */
    public static List<String> toResourceListFromEntitySet(Set<Role> entity) {
        return entity.stream()
                .map(Role::getStringName)
                .toList();
    }
}
