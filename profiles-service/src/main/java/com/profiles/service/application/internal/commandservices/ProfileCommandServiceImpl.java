package com.profiles.service.application.internal.commandservices;

import com.profiles.service.domain.model.aggregates.Profile;
import com.profiles.service.domain.model.commands.CreateProfileCommand;
import com.profiles.service.domain.model.commands.UpdateProfileCommand;
import com.profiles.service.domain.services.ProfileCommandService;
import com.profiles.service.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private final ProfileRepository userProfileRepository;

    public ProfileCommandServiceImpl(ProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        var userProfile = new Profile(command);
        var savedProfile = userProfileRepository.save(userProfile);
        return Optional.of(savedProfile);
    }

    @Override
    public Optional<Profile> handle(UpdateProfileCommand command) {
        Optional<Profile> existingProfile = userProfileRepository.findByUserId(command.userId());
        if (existingProfile.isEmpty()) { return Optional.empty(); }
        Profile profile = existingProfile.get();
        if (command.firstName() != null || command.lastName() != null) {
            String firstName = command.firstName() != null ? command.firstName() : profile.getFullName().split(" ")[0];
            String lastName = command.lastName() != null ? command.lastName() :
                              (profile.getFullName().split(" ").length > 1 ? profile.getFullName().split(" ")[1] : "");
            profile.updateName(firstName, lastName);
        }
        if (command.phoneNumber() != null) { profile.updatePhoneNumber(command.phoneNumber()); }
        var updatedProfile = userProfileRepository.save(profile);
        return Optional.of(updatedProfile);
    }
}
