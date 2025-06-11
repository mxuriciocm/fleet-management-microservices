package com.iam.service.domain.model.commands;

/**
 * Command to change user's email.
 * <p>
 *     This command is used to change the email address of a user.
 *     It contains the user ID, current password for verification,
 *     and new email to be set.
 * </p>
 */
public record ChangeEmailCommand(Long userId, String password, String newEmail) {
}
