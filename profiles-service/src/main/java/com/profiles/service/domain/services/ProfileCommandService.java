package com.profiles.service.domain.services;

import com.profiles.service.domain.model.aggregates.Profile;
import com.profiles.service.domain.model.commands.CreateProfileCommand;
import com.profiles.service.domain.model.commands.UpdateProfileCommand;

import java.util.Optional;

/**
 * UserProfile command service
 */
public interface ProfileCommandService {
    /**
     * Handle Create UserProfile Command
     *
     * @param command The {@link CreateProfileCommand} Command
     * @return An {@link Optional < Profile >} instance if the command is valid, otherwise empty
     * @throws IllegalArgumentException if the email address already exists
     */
    Optional<Profile> handle(CreateProfileCommand command);

    /**
     * Handle Update UserProfile Command
     *
     * @param command The {@link UpdateProfileCommand} Command
     * @return An {@link Optional< Profile >} instance if the profile was updated successfully, otherwise empty
     * @throws IllegalArgumentException if the profile doesn't exist
     */
    Optional<Profile> handle(UpdateProfileCommand command);
}