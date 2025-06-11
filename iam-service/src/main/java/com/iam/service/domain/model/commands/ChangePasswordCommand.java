package com.iam.service.domain.model.commands;

/**
 * Command to change user's password.
 * <p>
 *     This command is used to change the password of a user.
 *     It contains the user ID, current password for verification,
 *     and new password to be set.
 * </p>
 */
public record ChangePasswordCommand(Long userId, String currentPassword, String newPassword) {
}
