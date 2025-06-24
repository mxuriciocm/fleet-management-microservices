package com.iam.service.domain.model.commands;

import com.iam.service.domain.model.entities.Role;

import java.util.List;

/**
 * Command to register a new carrier by a manager.
 * <p>
 *     This command is used by managers to create new carrier users in the system.
 *     It contains the carrier's username, password, assigned roles, and the ID of the manager creating the carrier.
 * </p>
 */
public record RegisterCarrierCommand(String username, String password, List<Role> roles, Long managerId) {
}
