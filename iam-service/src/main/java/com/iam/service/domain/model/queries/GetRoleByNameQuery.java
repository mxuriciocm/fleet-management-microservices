package com.iam.service.domain.model.queries;

import com.iam.service.domain.model.valueobjects.Roles;

/**
 * Query to get a role by name.
 */
public record GetRoleByNameQuery(Roles roleName) {
}
