package com.iam.service.domain.services;

import com.iam.service.domain.model.entities.Role;
import com.iam.service.domain.model.queries.GetAllRolesQuery;
import com.iam.service.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

/**
 * RoleQueryService
 * <p>
 *     Service to handle role queries.
 * </p>
 */
public interface RoleQueryService {
    /**
     * Handle get all role queries.
     * <p>
     *     Get all roles query is used to get all roles in the system.
     * </p>
     *
     * @param query the {@link GetAllRolesQuery} query
     * @return the list of {@link Role} roles
     */
    List<Role> handle(GetAllRolesQuery query);

    /**
     * Handle get role by name query.
     * <p>
     *     Get role by name query is used to get a role by name in the system.
     * </p>
     *
     * @param query the {@link GetRoleByNameQuery} query
     * @return the {@link Role} role
     */
    Optional<Role> handle(GetRoleByNameQuery query);
 }
