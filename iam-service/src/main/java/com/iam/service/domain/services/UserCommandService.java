package com.iam.service.domain.services;

import com.iam.service.domain.model.aggregates.User;
import com.iam.service.domain.model.commands.ChangeEmailCommand;
import com.iam.service.domain.model.commands.ChangePasswordCommand;
import com.iam.service.domain.model.commands.RegisterCarrierCommand;
import com.iam.service.domain.model.commands.SignInCommand;
import com.iam.service.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * User command service.
 * <p>
 *     This service is responsible for handling user commands.
 *     It provides methods to handle sign-up and sign-in commands.
 * </p>
 */
public interface UserCommandService {
    /**
     * Handle sign-up command.
     *
     * @param command the command
     * @return an optional of user if the sign-up was successful
     */
    Optional<User> handle(SignUpCommand command);

    /**
     * Handle sign in command.
     *
     * @param command the command
     * @return an optional of user and token if the sign-in was successful
     */
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    /**
     * Handle change password command.
     *
     * @param command the command
     * @return an optional of user if the password change was successful
     * @throws RuntimeException if the current password is incorrect or user not found
     */
    Optional<User> handle(ChangePasswordCommand command);

    /**
     * Handle change email command.
     *
     * @param command the command
     * @return an optional of user if the email change was successful
     * @throws RuntimeException if the password is incorrect, user not found, or new email already exists
     */
    Optional<User> handle(ChangeEmailCommand command);

    /**
     * Handle register carrier command.
     *
     * @param command the command containing carrier details and manager ID
     * @return an optional of user if the carrier registration was successful
     */
    Optional<User> handle(RegisterCarrierCommand command);

    /**
     * Delete a user by ID.
     *
     * @param userId the ID of the user to delete
     * @return true if the user was deleted successfully, false otherwise
     * @throws RuntimeException if the user could not be deleted
     */
    boolean deleteUser(Long userId);
}
